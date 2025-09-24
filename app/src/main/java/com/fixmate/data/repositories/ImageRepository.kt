package com.fixmate.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.fixmate.data.remote.SupabaseClient
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) {

    suspend fun uploadProfileImage(imageUri: Uri, userId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert URI to compressed image bytes
                val imageBytes = uriToCompressedBytes(imageUri)

                // Generate unique filename
                val fileName = "profile_${userId}_${UUID.randomUUID()}.jpg"

                // Upload to Supabase
                val imageUrl = uploadToSupabase(imageBytes, fileName, SupabaseClient.PROFILE_IMAGES_BUCKET)

                Result.success(imageUrl)
            } catch (e: Exception) {
                Log.e("ImageRepository", "Error uploading image", e)
                Result.failure(e)
            }
        }
    }

    suspend fun uploadChatImage(imageUri: Uri, userId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert URI to compressed image bytes
                val imageBytes = uriToCompressedBytes(imageUri)

                // Generate unique filename
                val fileName = "chat_${userId}_${UUID.randomUUID()}.jpg"

                // Upload to Supabase
                val imageUrl = uploadToSupabase(imageBytes, fileName, SupabaseClient.CHAT_IMAGES_BUCKET)

                Result.success(imageUrl)
            } catch (e: Exception) {
                Log.e("ImageRepository", "Error uploading chat image", e)
                Result.failure(e)
            }
        }
    }

    private fun uriToCompressedBytes(uri: Uri): ByteArray {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Compress and resize image
        val maxWidth = 800
        val maxHeight = 800
        val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val ratio = Math.min(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()

        // Clean up
        scaledBitmap.recycle()
        if (scaledBitmap != bitmap) {
            bitmap.recycle()
        }

        return bytes
    }

    private suspend fun uploadToSupabase(imageBytes: ByteArray, fileName: String, bucketName: String): String {
        try {
            val bucket = supabaseClient.client.storage.from(bucketName)

            // Upload the file
            bucket.upload(fileName, imageBytes, upsert = true)

            // Get the public URL
            val publicUrl = bucket.publicUrl(fileName)

            Log.d("ImageRepository", "Successfully uploaded image: $publicUrl")
            return publicUrl

        } catch (e: Exception) {
            Log.e("ImageRepository", "Supabase upload failed", e)
            throw Exception("Failed to upload image to Supabase: ${e.message}")
        }
    }

    suspend fun deleteProfileImage(fileName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val bucket = supabaseClient.client.storage.from(SupabaseClient.PROFILE_IMAGES_BUCKET)
                bucket.delete(fileName)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ImageRepository", "Error deleting image", e)
                Result.failure(e)
            }
        }
    }
}