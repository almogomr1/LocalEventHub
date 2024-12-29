package com.localeventhub.app.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.Post

@Database(entities = [Post::class, Comment::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
}