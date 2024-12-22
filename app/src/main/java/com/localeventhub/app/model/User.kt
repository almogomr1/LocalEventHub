package com.localeventhub.app.model

import java.io.Serializable

data class User(
    var userId: String = "",
    var name: String = "",
    val email: String = "",
    var profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable
