package com.localeventhub.app.di

import com.google.firebase.auth.FirebaseAuth
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
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(auth: FirebaseAuth,firebaseFireStore: FirebaseFirestore): FirebaseRepository {
        return FirebaseRepositoryImpl(auth,firebaseFireStore)
    }

}