package com.localeventhub.app.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.model.DocumentCollections
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.localeventhub.app.model.User
import com.localeventhub.app.utils.Constants
import java.util.UUID
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore
) : FirebaseRepository {
    private val usersRef: CollectionReference = firebaseFireStore.collection("USERS")
    override fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Constants.loggedUserId = getLoggedUserId()
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    override fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthException -> {
                            // Map error codes to custom messages
                            when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                "ERROR_USER_NOT_FOUND" -> "No account found for this email. Please sign up first."
                                "ERROR_WRONG_PASSWORD" -> "The password is incorrect. Please try again."
                                "ERROR_USER_DISABLED" -> "This account has been disabled."
                                else -> "Authentication failed. Please try again."
                            }
                        }
                        else -> exception?.localizedMessage ?: "Unknown error occurred. Please try again."
                    }
                    callback(false, errorMessage)
                }
            }
    }

    override fun saveUserData(user: User,imageUri: Uri) {
        val userId = auth.currentUser?.uid
        user.userId = userId!!
        uploadImageToFirebaseStorage(imageUri) { success, downloadUrl ->
            if (success) {
                user.profileImageUrl = downloadUrl
                usersRef.document(userId).set(user)
                println("Image uploaded successfully. URL: $downloadUrl")
            } else {
                println("Failed to upload image: $downloadUrl")
            }
        }

    }

    override fun checkUserAuth(): Boolean {
        if(auth.currentUser != null){
            Constants.loggedUserId = auth.currentUser!!.uid
        }
        return auth.currentUser != null
    }

    override fun getLoggedUserId(): String {
        return auth.currentUser!!.uid
    }

    override fun fetchUserDetails(id: String) {
        firebaseFireStore.document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        Constants.loggedUser = user
                        Log.d("LOGGED_USER", user.toString())
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching user data", exception)
            }
    }

    override fun logout() {
        auth.signOut()
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val uniqueFileName = "images/users/${UUID.randomUUID()}.jpg" // Unique file path in the storage
        val imageRef = storageReference.child(uniqueFileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(true, downloadUri.toString())
                }.addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }

}