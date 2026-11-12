package com.example.ruangai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.ruangai.data.local.graphics.FurnitureCutoutBitmapFactory
import com.example.ruangai.domain.model.DimensionsCm

/**
 * Renders a transparent-background furniture cutout asset for Virtual Try-On and catalog previews.
 * Ensures zero opaque room background, accurate furniture silhouette, and physical aspect ratio.
 */
@Composable
fun FurnitureCutoutImage(
    productId: String,
    productTitle: String,
    categoryName: String,
    colorHex: Long = 0xFFD4B393,
    dimensions: DimensionsCm = DimensionsCm(100, 60, 75),
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(productId, categoryName, colorHex, dimensions) {
        FurnitureCutoutBitmapFactory.createTransparentCutoutBitmap(
            productId = productId,
            categoryName = categoryName,
            colorHex = colorHex,
            dimensions = dimensions,
            targetWidthPx = 360
        ).asImageBitmap()
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = productTitle,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
