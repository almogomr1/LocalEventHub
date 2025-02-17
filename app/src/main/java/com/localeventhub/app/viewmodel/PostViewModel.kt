package com.localeventhub.app.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.localeventhub.app.firebase.FirebaseRepository
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.Post
import com.localeventhub.app.repository.DatabaseRepository
import com.localeventhub.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val database: DatabaseRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts

    private val allPosts = mutableListOf<Post>()

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

    fun searchPostsByTag(query: String) {
        Log.d("PostViewModel", "searchPostsByTag called with query=$query")
        Log.d("PostViewModel", "All posts size: ${allPosts.size}")
        if (allPosts.isEmpty()) {
            Log.d("PostViewModel", "No posts available in allPosts")
            return // No posts available to search
        }
        _posts.value = if (query.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

}