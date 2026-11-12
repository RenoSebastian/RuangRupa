package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.*

data class AffiliateLinkRequestDto(
    val productId: String,
    val sceneId: String,
    val slotId: String? = null
)

data class AffiliateLinkResponseDto(
    val productId: String,
    val marketplace: String,
    val originalProductUrl: String,
    val generatedAffiliateUrl: String,
    val subId1: String,
    val subId2: String,
    val subId3: String,
    val status: String = "ACTIVE_SIMULATED",
    val disclosureLabel: String = "Tautan Afiliasi [Simulasi Demo]"
) {
    fun toDomain(): AffiliateRedirectInfo = AffiliateRedirectInfo(
        productId = productId,
        marketplace = runCatching { MarketplaceProvider.valueOf(marketplace.uppercase()) }.getOrDefault(MarketplaceProvider.SHOPEE),
        originalProductUrl = originalProductUrl,
        generatedAffiliateUrl = generatedAffiliateUrl,
        trackingMetadata = TrackingMetadata(subId1, subId1, subId2, subId3),
        status = runCatching { AffiliateLinkStatus.valueOf(status) }.getOrDefault(AffiliateLinkStatus.ACTIVE_SIMULATED),
        disclosureLabel = disclosureLabel
    )
}
