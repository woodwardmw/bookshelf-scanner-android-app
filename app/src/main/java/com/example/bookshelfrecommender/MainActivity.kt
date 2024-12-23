package com.example.bookshelfrecommender

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.asFlow
import com.example.bookshelfrecommender.ui.theme.BookshelfRecommenderTheme
import com.example.bookshelfrecommender.models.Book
import com.example.bookshelfrecommender.viewmodel.BookViewModel
import com.example.bookshelfrecommender.BuildConfig
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

class MainActivity : ComponentActivity() {
    private var photoUri: Uri? = null

    // Use the standard ViewModel approach
    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookshelfRecommenderTheme {
                MainScreen(
                    bookViewModel = bookViewModel,
                    onScanBookshelf = { openCamera() }
                )
            }
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                bookViewModel.fetchBooks(photoUri!!, this)
            }
        }


    private fun openCamera() {
        val photoFile = File(
            cacheDir, // Use internal cache directory
            "bookshelf_${System.currentTimeMillis()}.jpg"
        )

        if (!photoFile.exists()) {
            photoFile.createNewFile()
        }

        photoUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.provider",
            photoFile
        )

        // Log for debugging
        println("File absolute path: ${photoFile.absolutePath}")
        println("Photo URI: $photoUri")

        cameraLauncher.launch(photoUri)
    }

}


@Composable
fun MainScreen(
    bookViewModel: BookViewModel = viewModel(),
    onScanBookshelf: () -> Unit
) {
    val books by bookViewModel.books.asFlow().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Photo to Recommendations",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onScanBookshelf) {
            Text("Scan Bookshelf")
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (books.isNotEmpty()) {
            LazyColumn {
                items(books) { book ->
                    BookItem(book = book)
                }
            }
        } else {
            Text(text = "No recommendations yet.")
        }
    }
}

@Composable
fun BookItem(book: Book) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = book.title ?: "Unknown Title", style = MaterialTheme.typography.bodyLarge)
        Text(text = book.author ?: "Unknown Author(s)", style = MaterialTheme.typography.bodyMedium)
        Text(text = book.seriesPosition ?: "", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Rating: ${book.rating}"  ?: "No rating", style = MaterialTheme.typography.bodySmall)
        Text(text = book.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
    }
}
