package com.localeventhub.app.firebase

import android.net.Uri
import com.localeventhub.app.model.User

interface FirebaseRepository {
    fun signUp(email: String, password: String,callback: (Boolean, String?) -> Unit)
    fun signIn(email: String, password: String,callback: (Boolean, String?) -> Unit)
    fun saveUserData(user: User,imageUri:Uri)
    fun updateUserData(user: User,imageUri:Uri?,callback: (Boolean, String?) -> Unit)
    fun checkUserAuth():Boolean
    fun getLoggedUserId():String
    fun fetchUserDetails(id:String)
    fun logout()
//    fun addPost(post: Post, callback: (Boolean, String) -> Unit)
//    fun updatePost(post: Post, callback: (Boolean, String) -> Unit)
//    fun deletePost(post: Post, callback: (Boolean, String) -> Unit)
//    suspend fun syncPostsFromFireStore()
//    fun likePost(postId: String, userId: String, callback: (Boolean) -> Unit)
//    fun unlikePost(postId: String, userId: String, callback: (Boolean) -> Unit)
//    fun addComment(comment: Comment, callback: (Boolean, String?) -> Unit)
//    fun updateComment(comment: Comment, callback: (Boolean, String?) -> Unit)
//    fun deleteComment(comment: Comment, callback: (Boolean, String?) -> Unit)
//    fun syncCommentsForPost(postId: String)
//    fun observeLikes(postId: String, callback: (Int) -> Unit)
//    fun observeComments(postId: String, callback: (List<Comment>) -> Unit)
}