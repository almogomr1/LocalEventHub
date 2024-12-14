package com.localeventhub.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.localeventhub.app.firebase.FirebaseRepository
import com.localeventhub.app.firebase.FirebaseRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFireStoreRepository(firebaseFireStore: FirebaseFirestore): FirebaseRepository {
        return FirebaseRepositoryImpl(firebaseFireStore)
    }

}