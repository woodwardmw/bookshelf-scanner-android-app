package com.example.bookshelfrecommender

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bookshelfrecommender.models.Book
import com.example.bookshelfrecommender.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val books = remember { mutableStateOf<List<Book>>(emptyList()) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.weight(1f),
                onPhotoCaptured = { photoFile -> uploadPhoto(context, photoFile, books) }
            )
        } else {
            Text(
                text = "Camera permission is required to use this feature.",
                modifier = Modifier.padding(16.dp)
            )
        }

        if (books.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recommendations:", style = MaterialTheme.typography.headlineSmall)
            books.value.forEach { book: Book ->
                Text("- $book")
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPhotoCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context: android.content.Context ->
                val previewView = PreviewView(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            context as ComponentActivity,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
                previewView
            }
        )

        Button(
            onClick = {
                Log.d("CameraPreview", "Button Clicked")
                println("Button Clicked")
                val photoFile = File(
                    context.getExternalFilesDir(null),
                    "${System.currentTimeMillis()}.jpg"
                )

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()



                imageCapture.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("CameraPreview", "onImageSaved triggered for: ${photoFile.absolutePath}")
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Photo saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                            }
                            onPhotoCaptured(photoFile)
                            Log.d("CameraPreview", "onPhotoCaptured called for: ${photoFile.absolutePath}")
                        }


                    }
                )
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Capture Photo")
        }
    }
}

private fun uploadPhoto(context: android.content.Context, photoFile: File, books: MutableState<List<Book>>) {
    Log.d("uploadPhoto", "Preparing to upload photo: ${photoFile.absolutePath}")

    val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.apiService.uploadPhoto(photoPart)
            withContext(Dispatchers.Main) {
                if (response.success) {
                    // Assuming your API returns Book objects, update this line:
                    books.value = response.books.map { Book(it.title, it.description, it.rating, it.keywords, it.similar_books) }
                    Toast.makeText(context, "Uploaded successfully!", Toast.LENGTH_SHORT).show()
                    Log.d("uploadPhoto", "Upload successful: ${response.books}")
                } else {
                    Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    Log.e("uploadPhoto", "Upload failed: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("uploadPhoto", "Error uploading photo", e)
        }
    }
}



