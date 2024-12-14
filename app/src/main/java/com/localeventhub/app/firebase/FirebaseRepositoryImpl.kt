package com.localeventhub.app.firebase

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(private val firebaseFireStore: FirebaseFirestore) :FirebaseRepository {
}