package com.example.bookshelfrecommender.models

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val books: List<Book>
)