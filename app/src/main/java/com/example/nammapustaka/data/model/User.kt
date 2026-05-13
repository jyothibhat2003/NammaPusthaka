package com.example.nammapustaka.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student",
    val borrowedBooks: List<String> = emptyList()
)