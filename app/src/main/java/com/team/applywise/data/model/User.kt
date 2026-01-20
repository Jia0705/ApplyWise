package com.team.applywise.data.model

data class User(
    val uid: String = "",
//    val name: String = "",
    val email: String = "",
//    val photoURL: String = "",
    val createdAt: Long = System.currentTimeMillis()
)