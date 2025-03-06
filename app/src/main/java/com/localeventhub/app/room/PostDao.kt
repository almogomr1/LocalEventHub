package com.localeventhub.app.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.localeventhub.app.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Update
    fun updatePost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Query("SELECT * FROM posts WHERE post_id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    suspend fun getAllPosts(): List<Post>

    @Delete
    fun deletePost(post: Post)

    @Query("DELETE FROM posts")
    suspend fun clearPosts()

    @Query("UPDATE posts SET likedBy = :likedBy WHERE post_id = :postId")
    suspend fun updateLikedBy(postId: String, likedBy: String)

    @Query("SELECT tags FROM posts")
    fun getAllTags(): LiveData<List<String>>

}