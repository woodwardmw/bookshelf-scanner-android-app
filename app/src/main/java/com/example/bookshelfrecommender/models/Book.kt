package com.example.bookshelfrecommender.models

data class Book(
    val title: String?,
    val author: String?,
    val description: String?,
    val rating: Double?,
    val keywords: List<String>,
    val seriesPosition: String?,
    val similarBooks: List<SimilarBook>
)

data class SimilarBook(
    val title: String?,
    val author: String?
)
