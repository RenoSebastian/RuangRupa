package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.*

data class ProductResponseDto(
    val id: String,
    val title: String,
    val category: String,
    val style: String,
    val priceIdr: Long,
    val marketplace: String,
    val storeName: String,
    val storeBadge: String,
    val rating: Float,
    val reviewCount: Int,
    val imageUrl: String,
    val cutoutUrl: String,
    val dimensions: List<Int>, // [w, d, h]
    val material: String,
    val description: String,
    val compatibilityScore: Int = 95
) {
    fun toDomain(): Product = Product(
        id = id,
        title = title,
        category = runCatching { ProductCategory.valueOf(category) }.getOrDefault(ProductCategory.SOFA),
        designStyle = DesignStyle.values().find { it.id.equals(style, ignoreCase = true) } ?: DesignStyle.JAPANDI,
        priceIdr = priceIdr,
        marketplace = runCatching { MarketplaceProvider.valueOf(marketplace.uppercase()) }.getOrDefault(MarketplaceProvider.SHOPEE),
        storeName = storeName,
        storeBadge = storeBadge,
        rating = rating,
        reviewCount = reviewCount,
        imageUrl = imageUrl,
        transparentCutoutUrl = cutoutUrl,
        dimensions = DimensionsCm(
            width = dimensions.getOrElse(0) { 100 },
            depth = dimensions.getOrElse(1) { 60 },
            height = dimensions.getOrElse(2) { 75 }
        ),
        material = material,
        description = description,
        compatibilityScore = compatibilityScore
    )
}
