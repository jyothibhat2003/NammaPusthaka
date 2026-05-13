package com.example.nammapustaka.data.model

data class Book(
    val bookId: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val category: String = "",
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val createdBy: String = "",
    val createdAt: Long = 0L
)
