package com.localeventhub.app.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.localeventhub.app.room.Converters
import com.localeventhub.app.utils.Constants
import java.io.Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    var description: String,
    @ColumnInfo(name = "image_url") var imageUrl: String?,
    @Embedded var location: EventLocation?,
    val timestamp: Long = System.currentTimeMillis(),
    var likesCount: Int = 0,
    var likedBy: String = "[]",
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "tags")
    var tags: List<String> = listOf(),
    @Embedded val user: User? = Constants.loggedUser,
): Serializable{
    constructor():this("","","","",EventLocation(0.0,0.0,""))

    fun getLikedByList(): List<String> {
        return Gson().fromJson(likedBy, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    }

    fun setLikedByList(likedByList: List<String>) {
        likedBy = Gson().toJson(likedByList)
    }
}
