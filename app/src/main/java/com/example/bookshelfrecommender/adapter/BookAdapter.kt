package com.example.bookshelfrecommender.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelfrecommender.R
import com.example.bookshelfrecommender.models.Book

class BookAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val rating: TextView = view.findViewById(R.id.rating)
        val keywords: TextView = view.findViewById(R.id.keywords)
        val similarBooks: TextView = view.findViewById(R.id.similarBooks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.description.text = book.description
        holder.rating.text = "Rating: ${book.rating}"
        holder.keywords.text = "Keywords: ${book.keywords.joinToString(", ")}"
        holder.similarBooks.text = "Similar Books: ${
            book.similar_books.joinToString { "${it.title} by ${it.author}" }
        }"
    }

    override fun getItemCount(): Int = books.size
}
