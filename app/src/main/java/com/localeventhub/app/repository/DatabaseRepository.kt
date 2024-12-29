package com.localeventhub.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.localeventhub.app.model.Post
import com.localeventhub.app.room.CommentDao
import com.localeventhub.app.room.PostDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getPostId():String{
        return firestore.collection("POSTS").document().id
    }

    fun addPost(post: Post, callback: (Boolean, String) -> Unit) {
        firestore.collection("POSTS")
            .document(post.postId)
            .set(post)
            .addOnSuccessListener {
                postDao.insertPost(post) // Save post locally
                callback(true, "Post added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Error adding post: ${e.message}")
            }
    }

    fun updatePost(post: Post, callback: (Boolean, String) -> Unit) {
        val postRef = firestore.collection("POSTS").document(post.postId)
        postRef.set(post)
            .addOnSuccessListener {
                postDao.updatePost(post)
                callback(true, "Post updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Error updating post: ${e.message}")
            }
    }

    fun deletePost(post: Post, callback: (Boolean, String) -> Unit) {
        firestore.collection("POSTS").document(post.postId)
            .delete()
            .addOnSuccessListener {
                postDao.deletePost(post) // Delete locally
                callback(true, "Post deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Error deleting post: ${e.message}")
            }
    }


    suspend fun getCachedPosts(): List<Post> {
        return postDao.getAllPosts()
    }

    suspend fun syncPostsFromFireStore() {
        val posts = firestore.collection("POSTS").get().await().documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)
        }
        postDao.clearPosts()
        postDao.insertPosts(posts)
    }

}