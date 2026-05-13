package com.example.nammapustaka.data.model

data class IssueHistory(
    val historyId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val action: String = "",
    val createdAt: Long = 0L
)
