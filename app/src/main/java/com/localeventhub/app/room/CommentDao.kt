package com.localeventhub.app.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localeventhub.app.model.Comment

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    // Insert multiple comments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)

    @Query("SELECT * FROM comments WHERE post_id = :postId")
    suspend fun getCommentsByPostId(postId: String): List<Comment>

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): LiveData<List<Comment>>

    @Query("DELETE FROM comments WHERE comment_id = :commentId")
    suspend fun deleteCommentById(commentId: String)
}