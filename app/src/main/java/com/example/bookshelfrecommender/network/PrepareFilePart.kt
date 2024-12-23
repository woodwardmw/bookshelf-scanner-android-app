package com.example.bookshelfrecommender.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}
