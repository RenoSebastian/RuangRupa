package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.*

data class CreateRoomSceneRequestDto(
    val title: String,
    val roomType: String,
    val imageUrl: String,
    val targetBudgetIdr: Long = 5000000L
)

data class PlacementTransformDto(
    val translationX: Float,
    val translationY: Float,
    val scale: Float,
    val rotationDegrees: Float,
    val zIndex: Int
) {
    fun toDomain(): PlacementTransform = PlacementTransform(
        translationX = translationX,
        translationY = translationY,
        scale = scale,
        rotationDegrees = rotationDegrees,
        zIndex = zIndex
    )

    companion object {
        fun fromDomain(d: PlacementTransform): PlacementTransformDto = PlacementTransformDto(
            translationX = d.translationX,
            translationY = d.translationY,
            scale = d.scale,
            rotationDegrees = d.rotationDegrees,
            zIndex = d.zIndex
        )
    }
}

data class ProductPlacementDto(
    val placementId: String,
    val productId: String,
    val productTitle: String,
    val productPriceIdr: Long,
    val categoryName: String,
    val cutoutUrl: String,
    val dimensionsCm: List<Int>, // [width, depth, height]
    val transform: PlacementTransformDto
) {
    fun toDomain(): ProductPlacement = ProductPlacement(
        placementId = placementId,
        productId = productId,
        productTitle = productTitle,
        productPriceIdr = productPriceIdr,
        categoryName = categoryName,
        referenceImage = ProductReferenceImage(
            imageId = "ref_$placementId",
            productId = productId,
            transparentCutoutUri = cutoutUrl,
            trueDimensionsCm = DimensionsCm(
                width = dimensionsCm.getOrElse(0) { 100 },
                depth = dimensionsCm.getOrElse(1) { 60 },
                height = dimensionsCm.getOrElse(2) { 75 }
            )
        ),
        transform = transform.toDomain()
    )

    companion object {
        fun fromDomain(p: ProductPlacement): ProductPlacementDto = ProductPlacementDto(
            placementId = p.placementId,
            productId = p.productId,
            productTitle = p.productTitle,
            productPriceIdr = p.productPriceIdr,
            categoryName = p.categoryName,
            cutoutUrl = p.referenceImage.transparentCutoutUri,
            dimensionsCm = listOf(
                p.referenceImage.trueDimensionsCm.width,
                p.referenceImage.trueDimensionsCm.depth,
                p.referenceImage.trueDimensionsCm.height
            ),
            transform = PlacementTransformDto.fromDomain(p.transform)
        )
    }
}

data class RoomSceneResponseDto(
    val id: String,
    val title: String,
    val roomType: String,
    val originalImageUrl: String,
    val targetBudgetIdr: Long,
    val placements: List<ProductPlacementDto> = emptyList(),
    val updatedAt: Long
) {
    fun toDomain(): RoomScene = RoomScene(
        id = id,
        title = title,
        roomType = runCatching { RoomType.valueOf(roomType) }.getOrDefault(RoomType.LIVING_ROOM),
        originalImage = OriginalRoomImage(
            assetId = "orig_$id",
            localUri = originalImageUrl
        ),
        targetBudget = targetBudgetIdr,
        placements = placements.map { it.toDomain() },
        updatedAt = updatedAt
    )
}
