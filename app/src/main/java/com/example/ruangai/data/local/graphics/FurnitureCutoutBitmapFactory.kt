package com.example.ruangai.data.local.graphics

import android.graphics.*
import com.example.ruangai.domain.model.DimensionsCm
import com.example.ruangai.domain.model.ProductCategory

/**
 * Generates deterministic, transparent-background furniture cutout Bitmaps (ARGB_8888)
 * tailored to each product's category, primary color, and physical aspect ratio.
 * Ensures 100% transparent background (alpha = 0 outside furniture geometry)
 * for Virtual Try-On and SceneCompositor export.
 */
object FurnitureCutoutBitmapFactory {

    fun createTransparentCutoutBitmap(
        productId: String,
        categoryName: String,
        colorHex: Long = 0xFFD4B393,
        dimensions: DimensionsCm = DimensionsCm(100, 60, 75),
        targetWidthPx: Int = 400
    ): Bitmap {
        // Compute realistic aspect ratio from physical dimensions (width vs height/depth)
        val rawRatio = (dimensions.height.coerceAtLeast(35).toFloat() / dimensions.width.coerceAtLeast(30).toFloat())
            .coerceIn(0.45f, 1.65f)
        val width = targetWidthPx.coerceAtLeast(120)
        val height = (width * rawRatio).toInt().coerceAtLeast(120)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Strictly transparent background
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val baseColor = colorHex.toInt() or -0x1000000
        val darkerColor = darkenColor(baseColor, 0.78f)
        val lighterColor = lightenColor(baseColor, 1.15f)
        val woodColor = Color.rgb(168, 125, 86)
        val darkMetalColor = Color.rgb(45, 45, 48)

        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkerColor
            style = Paint.Style.FILL
        }
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lighterColor
            style = Paint.Style.FILL
        }
        val legPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (productId.contains("ind") || productId.contains("chair_01")) darkMetalColor else woodColor
            style = Paint.Style.FILL
        }
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(55, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkenColor(baseColor, 0.62f)
            style = Paint.Style.STROKE
            strokeWidth = (width * 0.012f).coerceAtLeast(2f)
        }

        val w = width.toFloat()
        val h = height.toFloat()

        // Soft ground contact shadow for realistic room placement
        canvas.drawOval(RectF(w * 0.08f, h * 0.88f, w * 0.92f, h * 0.98f), shadowPaint)

        val catUpper = categoryName.uppercase()
        when {
            catUpper.contains("SOFA") -> {
                // Wooden / Metal Legs
                canvas.drawRoundRect(RectF(w * 0.14f, h * 0.78f, w * 0.19f, h * 0.93f), 6f, 6f, legPaint)
                canvas.drawRoundRect(RectF(w * 0.81f, h * 0.78f, w * 0.86f, h * 0.93f), 6f, 6f, legPaint)
                // Backrest
                val backRect = RectF(w * 0.12f, h * 0.20f, w * 0.88f, h * 0.65f)
                canvas.drawRoundRect(backRect, 28f, 28f, shadePaint)
                canvas.drawRoundRect(backRect, 28f, 28f, strokePaint)
                // Left & Right Cushions
                canvas.drawRoundRect(RectF(w * 0.15f, h * 0.24f, w * 0.49f, h * 0.60f), 20f, 20f, mainPaint)
                canvas.drawRoundRect(RectF(w * 0.51f, h * 0.24f, w * 0.85f, h * 0.60f), 20f, 20f, mainPaint)
                // Armrests
                canvas.drawRoundRect(RectF(w * 0.06f, h * 0.38f, w * 0.17f, h * 0.80f), 22f, 22f, shadePaint)
                canvas.drawRoundRect(RectF(w * 0.83f, h * 0.38f, w * 0.94f, h * 0.80f), 22f, 22f, shadePaint)
                // Seat Base
                val seatRect = RectF(w * 0.12f, h * 0.56f, w * 0.88f, h * 0.82f)
                canvas.drawRoundRect(seatRect, 18f, 18f, highlightPaint)
                canvas.drawRoundRect(seatRect, 18f, 18f, strokePaint)
            }

            catUpper.contains("MEJA KOPI") || catUpper.contains("COFFEE") || productId.contains("table_01") -> {
                // Tripod / 4 angled wooden legs
                canvas.drawRoundRect(RectF(w * 0.22f, h * 0.42f, w * 0.28f, h * 0.92f), 8f, 8f, legPaint)
                canvas.drawRoundRect(RectF(w * 0.72f, h * 0.42f, w * 0.78f, h * 0.92f), 8f, 8f, legPaint)
                canvas.drawRoundRect(RectF(w * 0.47f, h * 0.45f, w * 0.53f, h * 0.89f), 8f, 8f, legPaint)
                // Tabletop Rim
                canvas.drawOval(RectF(w * 0.08f, h * 0.24f, w * 0.92f, h * 0.54f), shadePaint)
                // Tabletop Surface
                val topOval = RectF(w * 0.08f, h * 0.20f, w * 0.92f, h * 0.48f)
                canvas.drawOval(topOval, mainPaint)
                canvas.drawOval(topOval, strokePaint)
            }

            catUpper.contains("MEJA") || catUpper.contains("TABLE") || catUpper.contains("DINING") -> {
                // Desk / Dining table legs
                canvas.drawRect(RectF(w * 0.12f, h * 0.32f, w * 0.18f, h * 0.93f), legPaint)
                canvas.drawRect(RectF(w * 0.82f, h * 0.32f, w * 0.88f, h * 0.93f), legPaint)
                // Crossbar
                canvas.drawRect(RectF(w * 0.15f, h * 0.65f, w * 0.85f, h * 0.70f), shadePaint)
                // Tabletop
                val topRect = RectF(w * 0.06f, h * 0.22f, w * 0.94f, h * 0.35f)
                canvas.drawRoundRect(topRect, 12f, 12f, mainPaint)
                canvas.drawRoundRect(topRect, 12f, 12f, strokePaint)
            }

            catUpper.contains("KURSI") || catUpper.contains("CHAIR") -> {
                // Chair Legs
                canvas.drawRoundRect(RectF(w * 0.24f, h * 0.62f, w * 0.30f, h * 0.93f), 6f, 6f, legPaint)
                canvas.drawRoundRect(RectF(w * 0.70f, h * 0.62f, w * 0.76f, h * 0.93f), 6f, 6f, legPaint)
                // Backrest
                val backOval = RectF(w * 0.22f, h * 0.12f, w * 0.78f, h * 0.56f)
                canvas.drawRoundRect(backOval, 32f, 32f, mainPaint)
                canvas.drawRoundRect(backOval, 32f, 32f, strokePaint)
                // Seat cushion
                val seatRect = RectF(w * 0.18f, h * 0.52f, w * 0.82f, h * 0.68f)
                canvas.drawRoundRect(seatRect, 18f, 18f, highlightPaint)
                canvas.drawRoundRect(seatRect, 18f, 18f, strokePaint)
            }

            catUpper.contains("LAMPU") || catUpper.contains("LAMP") || catUpper.contains("LIGHT") -> {
                // Base & Pole
                canvas.drawOval(RectF(w * 0.30f, h * 0.84f, w * 0.70f, h * 0.94f), legPaint)
                canvas.drawRect(RectF(w * 0.47f, h * 0.34f, w * 0.53f, h * 0.89f), legPaint)
                // Warm Glow Aura
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(65, 255, 210, 115)
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(w * 0.50f, h * 0.26f, w * 0.32f, glowPaint)
                // Lamp Shade
                val shadeRect = RectF(w * 0.25f, h * 0.10f, w * 0.75f, h * 0.38f)
                canvas.drawRoundRect(shadeRect, 18f, 18f, mainPaint)
                canvas.drawRoundRect(shadeRect, 18f, 18f, strokePaint)
            }

            catUpper.contains("KARPET") || catUpper.contains("RUG") -> {
                // Perspective Floor Rug
                val outerRug = RectF(w * 0.05f, h * 0.35f, w * 0.95f, h * 0.90f)
                canvas.drawRoundRect(outerRug, 24f, 24f, mainPaint)
                canvas.drawRoundRect(outerRug, 24f, 24f, strokePaint)
                val innerRug = RectF(w * 0.14f, h * 0.44f, w * 0.86f, h * 0.81f)
                canvas.drawRoundRect(innerRug, 16f, 16f, highlightPaint)
            }

            catUpper.contains("KAMAR TIDUR") || catUpper.contains("BED") -> {
                // Headboard
                canvas.drawRoundRect(RectF(w * 0.10f, h * 0.18f, w * 0.90f, h * 0.52f), 16f, 16f, legPaint)
                // Pillows
                canvas.drawRoundRect(RectF(w * 0.18f, h * 0.36f, w * 0.46f, h * 0.52f), 14f, 14f, highlightPaint)
                canvas.drawRoundRect(RectF(w * 0.54f, h * 0.36f, w * 0.82f, h * 0.52f), 14f, 14f, highlightPaint)
                // Mattress & Frame
                val bedRect = RectF(w * 0.08f, h * 0.48f, w * 0.92f, h * 0.86f)
                canvas.drawRoundRect(bedRect, 20f, 20f, mainPaint)
                canvas.drawRoundRect(bedRect, 20f, 20f, strokePaint)
            }

            else -> {
                // Shelves / Storage / Decor
                val bodyRect = RectF(w * 0.14f, h * 0.16f, w * 0.86f, h * 0.86f)
                canvas.drawRoundRect(bodyRect, 18f, 18f, mainPaint)
                canvas.drawRoundRect(bodyRect, 18f, 18f, strokePaint)
                canvas.drawRoundRect(RectF(w * 0.20f, h * 0.24f, w * 0.80f, h * 0.48f), 10f, 10f, highlightPaint)
                canvas.drawRoundRect(RectF(w * 0.20f, h * 0.54f, w * 0.80f, h * 0.78f), 10f, 10f, shadePaint)
            }
        }

        return bitmap
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = ((Color.red(color) * factor).toInt()).coerceIn(0, 255)
        val g = ((Color.green(color) * factor).toInt()).coerceIn(0, 255)
        val b = ((Color.blue(color) * factor).toInt()).coerceIn(0, 255)
        return Color.argb(Color.alpha(color), r, g, b)
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val r = ((Color.red(color) * factor).toInt()).coerceIn(0, 255)
        val g = ((Color.green(color) * factor).toInt()).coerceIn(0, 255)
        val b = ((Color.blue(color) * factor).toInt()).coerceIn(0, 255)
        return Color.argb(Color.alpha(color), r, g, b)
    }
}
