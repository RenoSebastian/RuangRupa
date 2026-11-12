package com.example.ruangai.data.repository.local

import com.example.ruangai.data.local.db.dao.AffiliateClickDao
import com.example.ruangai.data.local.db.entity.AffiliateClickEntity
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.AffiliateLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PersistentAffiliateLinkRepository(
    private val affiliateClickDao: AffiliateClickDao
) : AffiliateLinkRepository {

    override suspend fun getAffiliateLink(
        productId: String,
        sceneId: String,
        placementId: String?
    ): Result<AffiliateRedirectInfo> {
        val prod = MockData.products.find { it.id == productId } ?: MockData.products.first()
        val clickId = "attr_sim_${UUID.randomUUID().toString().take(8)}"
        val slotId = placementId ?: "catalog_direct"
        val userHash = "usr_anon_8f92a1"
        val tracking = TrackingMetadata(
            projectId = sceneId,
            subId1 = sceneId,
            subId2 = userHash,
            subId3 = slotId
        )
        val info = AffiliateRedirectInfo(
            clickAttributionId = clickId,
            productId = productId,
            sceneId = sceneId,
            placementId = placementId,
            marketplace = prod.marketplace,
            originalProductUrl = prod.originalProductUrl,
            generatedAffiliateUrl = "simulated://affiliate.ruangai.local/redirect?pid=$productId&mp=${prod.marketplace.name}&sub_id1=$sceneId&sub_id2=$userHash&sub_id3=$slotId&attr=$clickId",
            trackingMetadata = tracking,
            redirectDestination = RedirectDestination(
                destinationType = "SIMULATED_AFFILIATE_GATEWAY",
                targetMarketplaceName = prod.marketplace.displayName,
                simulatedReferenceCode = "SIM-${prod.marketplace.name}-${productId.uppercase()}-$clickId",
                isSimulatedRedirect = true
            ),
            status = AffiliateLinkStatus.SIMULATED,
            disclosureLabel = "RuangAI dapat memperoleh komisi dari pembelian melalui tautan afiliasi."
        )
        return Result.success(info)
    }

    override suspend fun recordAffiliateClick(productId: String, marketplace: String): Result<Unit> {
        val info = getAffiliateLink(productId, "general_catalog", null).getOrThrow()
        val prod = MockData.products.find { it.id == productId }
        recordStructuredAffiliateClick(info, prod?.title ?: productId)
        return Result.success(Unit)
    }

    override suspend fun recordStructuredAffiliateClick(
        info: AffiliateRedirectInfo,
        productTitle: String
    ): Result<AffiliateClickRecord> {
        val entity = AffiliateClickEntity(
            clickAttributionId = info.clickAttributionId,
            productId = info.productId,
            productTitle = productTitle,
            marketplace = info.marketplace.name,
            sceneId = info.sceneId,
            placementId = info.placementId,
            subId1 = info.trackingMetadata.subId1,
            subId2 = info.trackingMetadata.subId2,
            subId3 = info.trackingMetadata.subId3,
            status = info.status.name,
            isSimulated = true,
            clickedAt = System.currentTimeMillis()
        )
        affiliateClickDao.insertClickLog(entity)
        return Result.success(
            AffiliateClickRecord(
                clickAttributionId = entity.clickAttributionId,
                productId = entity.productId,
                productTitle = entity.productTitle,
                marketplace = info.marketplace,
                sceneId = entity.sceneId,
                placementId = entity.placementId,
                subId1 = entity.subId1,
                subId2 = entity.subId2,
                subId3 = entity.subId3,
                status = info.status,
                isSimulated = true,
                clickedAt = entity.clickedAt
            )
        )
    }

    override fun getAffiliateClickHistory(): Flow<List<AffiliateClickRecord>> {
        return affiliateClickDao.getAllClickLogs().map { list ->
            list.map { entity ->
                AffiliateClickRecord(
                    clickAttributionId = entity.clickAttributionId,
                    productId = entity.productId,
                    productTitle = entity.productTitle,
                    marketplace = runCatching { MarketplaceProvider.valueOf(entity.marketplace) }.getOrDefault(MarketplaceProvider.SHOPEE),
                    sceneId = entity.sceneId,
                    placementId = entity.placementId,
                    subId1 = entity.subId1,
                    subId2 = entity.subId2,
                    subId3 = entity.subId3,
                    status = runCatching { AffiliateLinkStatus.valueOf(entity.status) }.getOrDefault(AffiliateLinkStatus.SIMULATED),
                    isSimulated = entity.isSimulated,
                    clickedAt = entity.clickedAt
                )
            }
        }
    }
}
