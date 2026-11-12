package com.example.ruangai.domain.model

enum class SenderRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class ConversationStatus {
    ACTIVE,
    ARCHIVED,
    ERROR
}

enum class MessageDeliveryStatus {
    SENDING,
    STREAMING,
    DELIVERED,
    ERROR
}

enum class AttachmentType(val displayName: String) {
    ROOM_PHOTO("Foto Ruangan"),
    PRODUCT_REFERENCE("Referensi Produk"),
    STYLE_MOODBOARD("Moodboard Gaya"),
    VOICE_INPUT_PLACEHOLDER("Rekaman Suara (Mendatang)")
}

data class ChatAttachment(
    val id: String,
    val type: AttachmentType = AttachmentType.ROOM_PHOTO,
    val uriOrPath: String,
    val label: String = "Lampiran Foto Ruangan",
    val mimeType: String = "image/jpeg",
    val width: Int = 0,
    val height: Int = 0,
    val fileExists: Boolean = true,
    val isSimulatedAnalysis: Boolean = true,
    val simulationNote: String = "Simulasi — Foto dilampirkan sebagai referensi visual lokal (belum dianalisis AI cloud)"
)

data class ProductRecommendationItem(
    val productId: String,
    val productTitle: String,
    val categoryName: String,
    val priceIdr: Long,
    val imageUrl: String,
    val transparentCutoutUri: String,
    val dimensions: DimensionsCm,
    val primaryStyle: DesignStyle,
    val compatibleStyles: List<DesignStyle> = emptyList(),
    val reason: String,
    val styleCompatibilityLabel: String,
    val fitsBudget: Boolean = true,
    val budgetDeltaIdr: Long = 0L,
    val isSimulationCatalog: Boolean = true
) {
    fun getFormattedPrice(): String = "Rp " + "%,d".format(priceIdr).replace(',', '.')
}

data class ConsultationConversation(
    val conversationId: String,
    val sceneId: String,
    val title: String,
    val selectedStyle: DesignStyle? = null,
    val customBudgetIdr: Long? = null,
    val status: ConversationStatus = ConversationStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ConsultationContext(
    val scene: RoomScene,
    val conversation: ConsultationConversation? = null,
    val analysisResult: RoomAnalysisResult? = null,
    val shoppingListItems: List<ShoppingCartItem> = emptyList(),
    val availableCatalog: List<Product> = emptyList()
) {
    val effectiveStyle: DesignStyle?
        get() = conversation?.selectedStyle ?: scene.preferredStyle ?: analysisResult?.suggestedDesignStyles?.firstOrNull()

    val effectiveBudgetIdr: Long
        get() = conversation?.customBudgetIdr ?: scene.targetBudget

    val placedItemsTotalIdr: Long
        get() = scene.placements.sumOf { it.productPriceIdr }

    val shoppingListSubtotalIdr: Long
        get() = shoppingListItems
            .filter { it.associatedSceneId == scene.id && it.isSelected }
            .sumOf { it.subtotalIdr }

    val remainingBudgetForSceneIdr: Long
        get() = (effectiveBudgetIdr - maxOf(placedItemsTotalIdr, shoppingListSubtotalIdr)).coerceAtLeast(0L)
}

data class StyleMoodboardProfile(
    val style: DesignStyle,
    val colorPaletteNamed: List<Pair<String, Long>>,
    val materialSuggestions: List<String>,
    val furnitureCharacteristics: List<String>,
    val lightingCharacteristics: List<String>,
    val spaceUtilizationTip: String,
    val exampleRoomImages: List<String>,
    val recommendedProductIds: List<String>
)

