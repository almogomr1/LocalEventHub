package com.localeventhub.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.localeventhub.app.repository.DatabaseRepository
import com.localeventhub.app.room.PostDao
import com.localeventhub.app.room.AppDatabase
import com.localeventhub.app.room.CommentDao
import com.localeventhub.app.room.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun providePostDao(appDatabase: AppDatabase): PostDao = appDatabase.postDao()

    @Provides
    fun provideCommentDao(appDatabase: AppDatabase): CommentDao = appDatabase.commentDao()

    @Provides
    fun provideNotificationDao(appDatabase: AppDatabase): NotificationDao = appDatabase.notificationDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "local_event_hub_database"
        )
            .allowMainThreadQueries()
//            .addMigrations(AppDatabase.MIGRATION_6_7)
            .build()

    @Provides
    fun provideDatabaseRepository(firestore: FirebaseFirestore, postDao: PostDao, commentDao: CommentDao,notificationDao: NotificationDao): DatabaseRepository = DatabaseRepository(firestore,postDao,commentDao,notificationDao)
}