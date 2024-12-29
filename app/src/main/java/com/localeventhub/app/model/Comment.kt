package com.localeventhub.app.model

import androidx.room.*
import java.io.Serializable

@Entity(
    tableName = "comments",
    foreignKeys = [ForeignKey(
        entity = Post::class,
        parentColumns = ["post_id"],
        childColumns = ["post_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Comment(
    @PrimaryKey @ColumnInfo(name = "comment_id") val commentId: String, // Primary Key
    @ColumnInfo(name = "post_id", index = true) val postId: String,      // Foreign Key to Post
    @ColumnInfo(name = "user_id") val userId: String,                   // Commenter's user ID
    val content: String,                                               // Comment content
    val timestamp: Long                                                // Timestamp
):Serializable{
    constructor():this("","","","",0)
}

