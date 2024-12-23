package com.example.bookshelfrecommender.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelfrecommender.models.Book
import com.example.bookshelfrecommender.network.ApiClient
import com.example.bookshelfrecommender.network.prepareFilePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream


fun getFileFromUri(context: Context, uri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    tempFile.outputStream().use { output ->
        inputStream?.copyTo(output)
    }
    return tempFile
}

class BookViewModel : ViewModel() {

    private val _books = MutableLiveData<List<Book>>() // Internal MutableLiveData
    val books: LiveData<List<Book>> get() = _books     // External LiveData

    fun fetchBooks(uri: Uri, context: Context) {
        val file = getFileFromUri(context, uri)

        Log.d("test", "Fetching book!")
        println("Fetching book!")

        viewModelScope.launch {
            try {
                if (!file.exists() || !file.canRead()) {
                    println("Error: File does not exist or cannot be read")
                    _books.postValue(emptyList())
                    return@launch
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                val response = ApiClient.apiService.uploadPhoto(photoPart)

                if (response.success) {
                    _books.postValue(response.books)
                } else {
                    println("API call failed: ${response.message}")
                    _books.postValue(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _books.postValue(emptyList())
            }
        }
    }

}



