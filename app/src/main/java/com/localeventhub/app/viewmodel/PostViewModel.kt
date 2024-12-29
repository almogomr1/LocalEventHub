package com.localeventhub.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.localeventhub.app.firebase.FirebaseRepository
import com.localeventhub.app.model.Post
import com.localeventhub.app.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository,private val database:DatabaseRepository): ViewModel(){

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts


    fun getPostId():String{
        return database.getPostId()
    }

    fun addPost(post: Post,callback: (Boolean,String) -> Unit) {
        viewModelScope.launch {
            database.addPost(post,callback)
        }
    }

    fun updatePost(post: Post,callback: (Boolean,String) -> Unit) {
        viewModelScope.launch {
            database.updatePost(post,callback)
        }
    }

    fun deletePost(post: Post,callback: (Boolean,String) -> Unit) {
            database.deletePost(post,callback)
    }

    fun loadPosts(isOnline: Boolean) {
        viewModelScope.launch {
            if (isOnline) {
                database.syncPostsFromFireStore()
            }
            _posts.value = database.getCachedPosts()
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

}