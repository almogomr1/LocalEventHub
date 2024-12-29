package com.localeventhub.app.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.localeventhub.app.room.Converters
import com.localeventhub.app.utils.Constants
import java.io.Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val description: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @Embedded val location: EventLocation?,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "tags")
    val tags: List<String> = listOf(),
    @Embedded val user: User? = Constants.loggedUser,
): Serializable{
    constructor():this("","","","",EventLocation(0.0,0.0,""))
}
