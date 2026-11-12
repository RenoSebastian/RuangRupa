package com.example.ruangai.data.local.db.mapper

import com.example.ruangai.data.local.db.entity.RoomPlacementEntity
import com.example.ruangai.data.local.db.entity.RoomSceneEntity
import com.example.ruangai.domain.model.*

object RoomSceneMapper {

    fun toDomain(
        entity: RoomSceneEntity,
        placements: List<RoomPlacementEntity> = emptyList()
    ): RoomScene {
        val roomTypeEnum = try {
            RoomType.valueOf(entity.roomType)
        } catch (e: Exception) {
            RoomType.OTHER
        }

        val preferredStyleEnum = entity.preferredStyle?.let { raw ->
            DesignStyle.values().find { it.name.equals(raw, ignoreCase = true) || it.id.equals(raw, ignoreCase = true) }
        }

        val userDimensions = if (entity.userDimWidthCm != null && entity.userDimDepthCm != null && entity.userDimHeightCm != null) {
            DimensionsCm(
                width = entity.userDimWidthCm,
                depth = entity.userDimDepthCm,
                height = entity.userDimHeightCm
            )
        } else {
            null
        }

        return RoomScene(
            id = entity.id,
            title = entity.title,
            roomType = roomTypeEnum,
            originalImage = OriginalRoomImage(
                assetId = "orig_${entity.id}",
                localUri = entity.previewImagePath.ifEmpty { entity.originalImagePath },
                width = entity.width,
                height = entity.height,
                capturedAt = entity.createdAt
            ),
            placements = placements.map { toDomain(it) },
            targetBudget = entity.targetBudget,
            preferredStyle = preferredStyleEnum,
            userDimensionsCm = userDimensions,
            activeMakeoverId = entity.activeMakeoverId,
            updatedAt = entity.updatedAt
        )
    }

    fun toDomain(entity: RoomPlacementEntity): ProductPlacement {
        return ProductPlacement(
            placementId = entity.placementId,
            productId = entity.productId,
            productTitle = entity.productTitle,
            productPriceIdr = entity.productPriceIdr,
            categoryName = entity.categoryName,
            referenceImage = ProductReferenceImage(
                imageId = "ref_${entity.placementId}",
                productId = entity.productId,
                transparentCutoutUri = entity.cutoutUri,
                trueDimensionsCm = DimensionsCm(
                    width = entity.dimWidthCm,
                    depth = entity.dimDepthCm,
                    height = entity.dimHeightCm
                )
            ),
            transform = PlacementTransform(
                translationX = entity.translationX,
                translationY = entity.translationY,
                scale = entity.scale,
                rotationDegrees = entity.rotationDegrees,
                zIndex = entity.zIndex
            )
        )
    }

    fun toEntity(scene: RoomScene): RoomSceneEntity {
        return RoomSceneEntity(
            id = scene.id,
            title = scene.title,
            roomType = scene.roomType.name,
            originalImagePath = scene.originalImage.localUri,
            previewImagePath = scene.originalImage.localUri,
            width = scene.originalImage.width,
            height = scene.originalImage.height,
            targetBudget = scene.targetBudget,
            userDimWidthCm = scene.userDimensionsCm?.width,
            userDimDepthCm = scene.userDimensionsCm?.depth,
            userDimHeightCm = scene.userDimensionsCm?.height,
            activeMakeoverId = scene.activeMakeoverId,
            preferredStyle = scene.preferredStyle?.name,
            createdAt = scene.originalImage.capturedAt,
            updatedAt = scene.updatedAt
        )
    }

    fun toEntity(sceneId: String, placement: ProductPlacement): RoomPlacementEntity {
        return RoomPlacementEntity(
            placementId = placement.placementId,
            sceneId = sceneId,
            productId = placement.productId,
            productTitle = placement.productTitle,
            productPriceIdr = placement.productPriceIdr,
            categoryName = placement.categoryName,
            cutoutUri = placement.referenceImage.transparentCutoutUri,
            dimWidthCm = placement.referenceImage.trueDimensionsCm.width,
            dimDepthCm = placement.referenceImage.trueDimensionsCm.depth,
            dimHeightCm = placement.referenceImage.trueDimensionsCm.height,
            translationX = placement.transform.translationX,
            translationY = placement.transform.translationY,
            scale = placement.transform.scale,
            rotationDegrees = placement.transform.rotationDegrees,
            zIndex = placement.transform.zIndex
        )
    }
}
