package com.example.halalyticscompose.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.halalyticscompose.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"
    private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    
    fun normalizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        // If it's already a full URL (http or https), return it
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        
        // Prepend base URL for relative paths
        val baseUrl = BuildConfig.API_BASE_URL
            .removeSuffix("/api/")
            .removeSuffix("/api")
            .ifBlank { DEFAULT_BASE_URL }
            
        // Smart Storage Prefix:
        // If it's a relative path and doesn't start with 'storage/', prepend it 
        // (common for Laravel storage uploads)
        var finalPath = url
        if (!finalPath.startsWith("storage/") && !finalPath.startsWith("/storage/")) {
            // Only prepend storage if it looks like an uploaded file path (not a root asset)
            if (finalPath.contains("/") || finalPath.contains(".")) {
                finalPath = "storage/$finalPath"
            }
        }

        return if (finalPath.startsWith("/")) {
            "$baseUrl$finalPath"
        } else {
            "$baseUrl/$finalPath"
        }
    }

    /**
     * Convert a content URI to a temporary File.
     */
    fun uriToFile(uri: Uri, context: Context): File {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return tempFile
    }

    /**
     * Create a temporary URI for capturing images with camera.
     */
    fun createTempImageUri(context: Context, prefix: String = "camera"): Uri {
        val tempFile = File.createTempFile("${prefix}_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Reduce file image size by compressing it until it's under MAX_IMAGE_SIZE.
     */
    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return file
        var compressQuality = 90
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 10
        } while (streamLength > MAX_IMAGE_SIZE && compressQuality > 10)

        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality.coerceAtLeast(10), bmpStream)
        FileOutputStream(file).use { fos ->
            fos.write(bmpStream.toByteArray())
        }
        return file
    }
}
