package com.localeventhub.app.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.EventLocation
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post
import com.localeventhub.app.model.User
import com.localeventhub.app.room.CommentDao
import com.localeventhub.app.room.NotificationDao
import com.localeventhub.app.room.PostDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val notificationDao: NotificationDao,
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

    fun updatePostLikes(postId: String, likedByList: List<String>, callback: (Boolean, String) -> Unit) {
        val postRef = firestore.collection("POSTS").document(postId)

        postRef.update("likedBy", likedByList)
            .addOnSuccessListener {
                val likedByJson = Gson().toJson(likedByList)
                CoroutineScope(Dispatchers.IO).launch {
                    postDao.updateLikedBy(postId,likedByJson)
                }
                callback(true, "Likes updated successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Error updating likes")
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
        val posts = firestore.collection("POSTS").get().await().documents.mapNotNull { document ->
            document.toPost()
        }
        postDao.clearPosts()
        postDao.insertPosts(posts)
    }

    fun DocumentSnapshot.toPost(): Post? {
        return try {
            val postId = getString("postId") ?: return null
            val userId = getString("userId") ?: return null
            val description = getString("description") ?: ""
            val imageUrl = getString("imageUrl")
            val location = get("location") as? Map<String, Any> // Assuming EventLocation is mapped
            val timestamp = getLong("timestamp") ?: System.currentTimeMillis()
            val likesCount = getLong("likesCount")?.toInt() ?: 0
            val likedByList = get("likedBy") as? List<String> ?: emptyList()
            val user = get("user") as? Map<String, Any> // Assuming User is mapped
            val tags = get("tags") as? List<String> ?: emptyList()

            val eventLocation = location?.let {
                EventLocation(
                    it["latitude"] as? Double ?: 0.0,
                    it["longitude"] as? Double ?: 0.0,
                    it["address"] as? String ?: ""
                )
            }

            val loggedUser = user?.let {
                User(
                    userId = it["userId"] as? String ?: "",
                    name = it["name"] as? String ?: "",
                    profileImageUrl = it["profileImageUrl"] as? String ?: ""
                )
            }

            Post(
                postId = postId,
                userId = userId,
                description = description,
                imageUrl = imageUrl,
                location = eventLocation,
                timestamp = timestamp,
                likesCount = likesCount,
                likedBy = Gson().toJson(likedByList), // Convert List<String> to JSON String
                tags = tags,
                user = loggedUser
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addComment(comment: Comment, callback: (Boolean, String) -> Unit) {
        val commentRef = firestore.collection("COMMENTS").document(comment.commentId)
        commentRef.set(comment)
            .addOnSuccessListener {
                CoroutineScope(ioDispatcher).launch {
                    commentDao.insertComment(comment) // Save comment locally
                }
                callback(true, "Comment added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Error adding comment: ${e.message}")
            }
    }

    fun deleteComment(commentId: String, callback: (Boolean, String) -> Unit) {
        firestore.collection("COMMENTS").document(commentId)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(ioDispatcher).launch {
                    commentDao.deleteCommentById(commentId) // Delete comment locally
                }
                callback(true, "Comment deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Error deleting comment: ${e.message}")
            }
    }

    suspend fun syncCommentsForPost(postId: String) {
        val comments = firestore.collection("COMMENTS")
            .whereEqualTo("postId", postId)
            .get()
            .await()
            .documents.mapNotNull { it.toComment() }

        CoroutineScope(ioDispatcher).launch {
            commentDao.insertComments(comments)
        }
    }

    private fun DocumentSnapshot.toComment(): Comment? {
        return try {
            Comment(
                commentId = getString("commentId") ?: "",
                postId = getString("postId") ?: "",
                userId = getString("userId") ?: "",
                content = getString("content") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsForPost(postId)
    }

    fun getAllNotifications(): List<Notification> {
        return notificationDao.getAllNotifications()
    }

    suspend fun insertNotification(notification: Notification) {
        notificationDao.insertNotification(notification)
    }

    fun saveNotificationToFireStore(notification: Notification, onComplete: (Boolean) -> Unit) {
        firestore.collection("NOTIFICATIONS")
            .document(notification.id)
            .set(notification)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun syncNotificationsFromFireStore(receiverId: String) {
        firestore.collection("NOTIFICATIONS")
            .whereEqualTo("receiverId", receiverId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val notifications = result.toObjects(Notification::class.java)
                CoroutineScope(Dispatchers.IO).launch {
                    notificationDao.insertNotifications(notifications)
                }
            }
    }
}