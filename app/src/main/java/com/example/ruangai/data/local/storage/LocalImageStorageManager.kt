package com.example.ruangai.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Handles persistent local storage of room photos, immutable originals,
 * optimized downscaled preview copies, and asset cleanup on project deletion.
 */
class LocalImageStorageManager(private val context: Context) {

    private val imagesDir: File by lazy {
        File(context.filesDir, "room_images").apply {
            if (!exists()) mkdirs()
        }
    }

    private val cameraTempDir: File by lazy {
        File(context.cacheDir, "camera_temp").apply {
            if (!exists()) mkdirs()
        }
    }

    private val chatAttachmentsDir: File by lazy {
        File(context.filesDir, "chat_attachments").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Creates a temporary file and FileProvider Uri for camera capture.
     */
    fun createCameraTempUri(): Pair<File, Uri> {
        val file = File(cameraTempDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    /**
     * Copies source image (from content:// or file://) to durable internal storage.
     * Saves both immutable original and optimized preview.
     * Returns Pair(originalFilePath, previewFilePath).
     */
    suspend fun saveRoomImage(sceneId: String, sourceUri: Uri): SavedImageResult = withContext(Dispatchers.IO) {
        val originalFile = File(imagesDir, "${sceneId}_orig.jpg")
        val previewFile = File(imagesDir, "${sceneId}_prev.jpg")

        // 1. Copy full resolution original
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Cannot read source URI: $sourceUri")

        // 2. Decode with orientation fix & create downscaled preview copy
        val (width, height) = generateOptimizedPreview(originalFile, previewFile)

        SavedImageResult(
            originalFilePath = originalFile.absolutePath,
            previewFilePath = previewFile.absolutePath,
            width = width,
            height = height
        )
    }

    /**
     * Saves an additional reference angle photo for a room scene.
     */
    suspend fun saveReferenceImage(sceneId: String, refIndex: Int, sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val refFile = File(imagesDir, "${sceneId}_ref_${refIndex}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(refFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Cannot read reference URI: $sourceUri")
        refFile.absolutePath
    }

    /**
     * Generates an orientation-corrected, max 1280px preview copy.
     */
    private fun generateOptimizedPreview(originalFile: File, previewFile: File): Pair<Int, Int> {
        val rotationDegrees = getExifOrientation(originalFile)

        // Read dimensions first
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(originalFile.absolutePath, options)
        val origW = options.outWidth
        val origH = options.outHeight

        // Compute sample size to avoid OOM
        val maxDim = 1280
        var sampleSize = 1
        while (origW / sampleSize > maxDim * 2 || origH / sampleSize > maxDim * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodedBitmap = BitmapFactory.decodeFile(originalFile.absolutePath, decodeOptions)
            ?: throw IllegalStateException("Failed to decode image: ${originalFile.absolutePath}")

        // Apply rotation if needed
        val orientedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true).also {
                if (it != decodedBitmap) decodedBitmap.recycle()
            }
        } else {
            decodedBitmap
        }

        // Scale to max 1280px
        val finalBitmap = if (orientedBitmap.width > maxDim || orientedBitmap.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / orientedBitmap.width, maxDim.toFloat() / orientedBitmap.height)
            val newW = (orientedBitmap.width * ratio).toInt()
            val newH = (orientedBitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(orientedBitmap, newW, newH, true).also {
                if (it != orientedBitmap) orientedBitmap.recycle()
            }
        } else {
            orientedBitmap
        }

        // Save preview copy (85% JPEG quality)
        FileOutputStream(previewFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        val resultWidth = finalBitmap.width
        val resultHeight = finalBitmap.height
        finalBitmap.recycle()

        return Pair(resultWidth, resultHeight)
    }

    private fun getExifOrientation(file: File): Int {
        return try {
            val exif = ExifInterface(file.absolutePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Safely cleans up all image files associated with a sceneId when project is deleted.
     */
    fun deleteSceneAssets(sceneId: String) {
        imagesDir.listFiles { file ->
            file.name.startsWith("${sceneId}_")
        }?.forEach { it.delete() }
        chatAttachmentsDir.listFiles { dir ->
            dir.name.contains(sceneId)
        }?.forEach { it.deleteRecursively() }
    }

    /**
     * Copies a chat attachment image into durable internal storage under filesDir/chat_attachments/{conversationId}/
     * Never stores image Base64 payloads in SQLite.
     */
    suspend fun saveChatAttachmentImage(
        conversationId: String,
        sourceUriOrPath: String
    ): SavedChatAttachmentResult = withContext(Dispatchers.IO) {
        val convDir = File(chatAttachmentsDir, conversationId).apply {
            if (!exists()) mkdirs()
        }
        val destFile = File(convDir, "att_${System.currentTimeMillis()}.jpg")

        val copied = try {
            when {
                sourceUriOrPath.startsWith("content://") -> {
                    context.contentResolver.openInputStream(Uri.parse(sourceUriOrPath))?.use { input ->
                        FileOutputStream(destFile).use { output -> input.copyTo(output) }
                    } != null
                }
                sourceUriOrPath.startsWith("file://") -> {
                    val srcFile = File(sourceUriOrPath.removePrefix("file://"))
                    if (srcFile.exists()) {
                        srcFile.copyTo(destFile, overwrite = true)
                        true
                    } else false
                }
                else -> {
                    val srcFile = File(sourceUriOrPath)
                    if (srcFile.exists()) {
                        srcFile.copyTo(destFile, overwrite = true)
                        true
                    } else false
                }
            }
        } catch (e: Exception) {
            false
        }

        if (copied && destFile.exists() && destFile.length() > 0L) {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(destFile.absolutePath, opts)
            SavedChatAttachmentResult(
                localFilePath = destFile.absolutePath,
                width = opts.outWidth.coerceAtLeast(0),
                height = opts.outHeight.coerceAtLeast(0),
                fileExists = true
            )
        } else {
            // If source is an external/demo URI or copy could not read raw bytes, preserve reference safely
            val isRemoteOrContent = sourceUriOrPath.startsWith("http://") ||
                sourceUriOrPath.startsWith("https://") ||
                sourceUriOrPath.startsWith("content://")
            SavedChatAttachmentResult(
                localFilePath = sourceUriOrPath,
                width = 1280,
                height = 960,
                fileExists = isRemoteOrContent
            )
        }
    }

    /**
     * Deletes all stored attachment files for a specific conversationId.
     */
    fun deleteConversationAttachments(conversationId: String) {
        val convDir = File(chatAttachmentsDir, conversationId)
        if (convDir.exists()) {
            convDir.deleteRecursively()
        }
    }
}

data class SavedImageResult(
    val originalFilePath: String,
    val previewFilePath: String,
    val width: Int,
    val height: Int
)

data class SavedChatAttachmentResult(
    val localFilePath: String,
    val width: Int,
    val height: Int,
    val fileExists: Boolean
)
