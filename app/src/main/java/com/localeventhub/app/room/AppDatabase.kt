package com.localeventhub.app.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post

@Database(entities = [Post::class, Comment::class,Notification::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun notificationDao(): NotificationDao

    companion object{
//        val MIGRATION_6_7 = object : Migration(6, 7) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE posts ADD COLUMN likedBy TEXT NOT NULL DEFAULT '[]'")
//            }
//        }
    }
}