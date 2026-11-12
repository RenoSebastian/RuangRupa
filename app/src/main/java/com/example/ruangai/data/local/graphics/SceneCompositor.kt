package com.example.ruangai.data.local.graphics

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.example.ruangai.domain.model.DimensionsCm
import com.example.ruangai.domain.model.ProductPlacement
import com.example.ruangai.domain.model.RoomScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

data class CompositeExportResult(
    val compositeFilePath: String,
    val maskFilePath: String?,
    val width: Int,
    val height: Int,
    val exportedAt: Long = System.currentTimeMillis()
) {
    val compositeFile: File get() = File(compositeFilePath)
    val maskFile: File get() = File(maskFilePath ?: compositeFilePath)
    val fileSizeBytes: Long get() = compositeFile.length()
}

/**
 * Native Android Graphics Compositor:
 * Composites transparent furniture PNGs on top of immutable original room photo.
 * Generates flattened export JPEG and binary placement mask for future AI inpainting.
 */
class SceneCompositor(private val context: Context) {

    private val exportDir: File by lazy {
        File(context.filesDir, "room_exports").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Composites original room photo + multiple furniture layers with transforms.
     * Original image file is never modified or mutated.
     */
    suspend fun compositeScene(scene: RoomScene): CompositeExportResult = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmapFromUri(scene.originalImage.localUri)
            ?: throw IllegalStateException("Cannot load original room image: ${scene.originalImage.localUri}")

        val width = originalBitmap.width
        val height = originalBitmap.height

        // 1. Create mutable composite bitmap
        val compositeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val compositeCanvas = Canvas(compositeBitmap)

        // Draw original room immutable background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        compositeCanvas.drawBitmap(originalBitmap, 0f, 0f, bgPaint)

        // 2. Create inpainting mask bitmap (Black = protected, White = editable placement)
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        maskCanvas.drawColor(Color.BLACK)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // 3. Sort placements by zIndex and render
        val sortedPlacements = scene.placements.sortedBy { it.transform.zIndex }

        for (placement in sortedPlacements) {
            val cutoutBitmap = loadBitmapFromUri(placement.referenceImage.transparentCutoutUri)
                ?: FurnitureCutoutBitmapFactory.createTransparentCutoutBitmap(
                    productId = placement.productId,
                    categoryName = placement.categoryName,
                    dimensions = placement.referenceImage.trueDimensionsCm
                )

            // Compute placement center on canvas (scaled from UI preview coordinate system)
            val centerX = (width / 2f) + (placement.transform.translationX * (width / 360f))
            val centerY = (height / 2f) + (placement.transform.translationY * (height / 340f))

            // Standard furniture item base size mapped to room scale
            val baseItemSize = width * 0.35f
            val itemWidth = baseItemSize * placement.transform.scale
            val itemHeight = (cutoutBitmap.height.toFloat() / cutoutBitmap.width.toFloat()) * itemWidth

            val matrix = Matrix().apply {
                // Scale cutout to target dimension
                postScale(itemWidth / cutoutBitmap.width.toFloat(), itemHeight / cutoutBitmap.height.toFloat())
                // Rotate around its center
                postRotate(placement.transform.rotationDegrees, itemWidth / 2f, itemHeight / 2f)
                // Position centered on (centerX, centerY)
                postTranslate(centerX - (itemWidth / 2f), centerY - (itemHeight / 2f))
            }

            // Draw furniture cutout onto composite
            compositeCanvas.drawBitmap(cutoutBitmap, matrix, bgPaint)

            // Draw bounding region to inpainting mask
            val boundsRect = RectF(
                centerX - (itemWidth / 2f) * 1.05f,
                centerY - (itemHeight / 2f) * 1.05f,
                centerX + (itemWidth / 2f) * 1.05f,
                centerY + (itemHeight / 2f) * 1.05f
            )
            maskCanvas.drawRoundRect(boundsRect, 16f, 16f, maskPaint)

            cutoutBitmap.recycle()
        }

        // 4. Save flattened composite image
        val compositeFile = File(exportDir, "${scene.id}_composite_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compositeFile).use { out ->
            compositeBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        // 5. Save mask image
        val maskFile = File(exportDir, "${scene.id}_mask_${System.currentTimeMillis()}.png")
        FileOutputStream(maskFile).use { out ->
            maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        originalBitmap.recycle()
        compositeBitmap.recycle()
        maskBitmap.recycle()

        CompositeExportResult(
            compositeFilePath = compositeFile.absolutePath,
            maskFilePath = maskFile.absolutePath,
            width = width,
            height = height
        )
    }

    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            when {
                uriString.startsWith("content://") || uriString.startsWith("file://") -> {
                    context.contentResolver.openInputStream(Uri.parse(uriString))?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                uriString.startsWith("http://") || uriString.startsWith("https://") -> {
                    // For remote demo image URLs
                    val url = URL(uriString)
                    url.openStream()?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                File(uriString).exists() -> {
                    BitmapFactory.decodeFile(uriString)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
