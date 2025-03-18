package com.localeventhub.app.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.localeventhub.app.firebase.FirebaseRepository
import com.localeventhub.app.model.ChatGptRequest
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.Message
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post
import com.localeventhub.app.repository.DatabaseRepository
import com.localeventhub.app.retrofit.ChatGptApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val database: DatabaseRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> get() = _notifications

    private val allPosts = mutableListOf<Post>()

    val tags: LiveData<List<String>> = database.getAllUniqueTags()

    fun getPostId(): String {
        return database.getPostId()
    }

    fun addPost(post: Post, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            database.addPost(post, callback)
        }
    }

    fun updatePost(post: Post, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            database.updatePost(post, callback)
        }
    }

    fun deletePost(post: Post, callback: (Boolean, String) -> Unit) {
        database.deletePost(post, callback)
    }

    fun loadPosts(isOnline: Boolean) {
        viewModelScope.launch {
            if (isOnline) {
                database.syncPostsFromFireStore()
            }
            val cachedPosts = database.getCachedPosts()
            if (cachedPosts.isNotEmpty()) {
                allPosts.clear()
                allPosts.addAll(cachedPosts)
            }
            _posts.value = allPosts.toList()
        }
    }

    fun deleteImageFromFirebaseStorage(imageUrl: String, callback: (Boolean, String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.delete()
            .addOnSuccessListener {
                callback(true, "Image Deleted Successfully")
            }
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val uniqueFileName = "images/posts/${UUID.randomUUID()}.jpg"
        val imageRef = storageReference.child(uniqueFileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(true, downloadUri.toString())
                }.addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }

    fun likePost(post: Post, userId: String,callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            post.let {
                val likedByList = it.getLikedByList().toMutableList()
                if (likedByList.contains(userId)) {
                    likedByList.remove(userId)
                } else {
                    likedByList.add(userId)
                }

                database.updatePostLikes(post.postId, likedByList) { success, message ->
                    if (success) {
                        callback(true,"")
                    }
                }
            }
        }
    }

    fun addComment(comment: Comment, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            database.addComment(comment, callback)
        }
    }

    fun deleteComment(commentId: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            database.deleteComment(commentId, callback)
        }
    }

    fun loadCommentsForPost(postId: String) {
        viewModelScope.launch {
            database.syncCommentsForPost(postId)
        }
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return database.getCommentsForPost(postId)
    }

    fun loadNotifications(isOnline: Boolean,receiverId: String) {
        viewModelScope.launch {
            if (isOnline) {
                database.syncNotificationsFromFireStore(receiverId)
            }
            _notifications.value =  database.getAllNotifications(receiverId)

        }
    }

    fun getAllNotifications(receiverId:String): List<Notification> {
        return database.getAllNotifications(receiverId)
    }

    fun saveNotification(notification: Notification) {
        viewModelScope.launch {
            database.insertNotification(notification) // Save to Room
            database.saveNotificationToFireStore(notification) { success ->
                if (!success) {
                    Log.e("Notification", "Failed to save notification to Firestore")
                }
            }
        }
    }

    fun syncNotifications(receiverId: String) {
        database.syncNotificationsFromFireStore(receiverId)
    }

    fun searchPostsByTag(query: String) {
        if (allPosts.isEmpty()) {
            return
        }
        _posts.value = if (query.isEmpty() or (query.lowercase() == "all")) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }


    suspend fun callAiRecommendationRequest(prompt: String,callback:(String)->Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val chatGptApi = retrofit.create(ChatGptApi::class.java)

        viewModelScope.launch {
            val apiKey = ""

            val recommendations = getChatGptRecommendations(apiKey, prompt,chatGptApi)

            recommendations?.let {
                println(it)
                callback(it)
            } ?: run {
                callback("Failed to get recommendations")
            }

        }
    }

    private suspend fun getChatGptRecommendations(apiKey: String, userInput: String, chatGptApi: ChatGptApi): String? {
        val request = ChatGptRequest(
            model = "gpt-3.5-turbo", // Use the appropriate model name
            messages = listOf(
                Message(role = "user", content = userInput)
            )
        )

        val response = chatGptApi.getRecommendations("Bearer $apiKey", request)

        return if (response.isSuccessful) {
            response.body()?.choices?.firstOrNull()?.message?.content
        } else {
            // Handle the error
            null
        }
    }


}