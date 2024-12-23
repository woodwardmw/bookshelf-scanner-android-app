package com.example.bookshelfrecommender.network

import com.example.bookshelfrecommender.models.ApiResponse


import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// Define the API Service
interface ApiService {
    @Multipart
    @POST("/process-image/") // Replace with your API endpoint
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): ApiResponse
}


// Create Retrofit Client
object ApiClient {
    private const val BASE_URL = "https://web-n7swu8v9knls.up-de-fra1-k8s-1.apps.run-on-seenode.com" // Replace with your API URL
//    private const val BASE_URL = "https://ecda-2601-600-c87f-4370-c459-3a51-5250-f5c1.ngrok-free.app"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Increase connection timeout
        .readTimeout(60, TimeUnit.SECONDS)    // Increase read timeout
        .writeTimeout(60, TimeUnit.SECONDS)   // Increase write timeout
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}