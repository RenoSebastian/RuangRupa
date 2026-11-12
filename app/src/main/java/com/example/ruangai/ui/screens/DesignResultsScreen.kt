package com.example.ruangai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.data.repository.mock.ConsultantRecommendationEngine
import com.example.ruangai.data.repository.mock.DeterministicMakeoverGenerator
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.*
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.BeforeAfterSlider
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignResultsScreen(
    scene: RoomScene,
    onBack: () -> Unit,
    onOpenTryOn: (String) -> Unit,
    onOpenShopping: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
    allScenes: List<RoomScene> = listOf(scene),
    allCatalogProducts: List<Product> = MockData.products,
    styleMoodboards: List<StyleMoodboardProfile> = remember {
        ConsultantRecommendationEngine().getAllStyleMoodboardProfiles()
    },
    activeDesignResult: DesignGenerationResult? = null,
    designHistory: List<DesignGenerationResult> = emptyList(),
    favoriteVariations: List<DesignVariation> = emptyList(),
    creditAvailable: Int = 3,
    isGeneratingMakeover: Boolean = false,
    makeoverError: String? = null,
    lastFailedRequestId: String? = null,
    statusNotice: String? = null,
    initialTab: Int = 0,
    onSelectScene: (String) -> Unit = {},
    onSelectVariation: (resultId: String, variationId: String) -> Unit = { _, _ -> },
    onToggleFavoriteVariation: (variationId: String, isFavorite: Boolean) -> Unit = { _, _ -> },
    onSelectHistoryResult: (resultId: String) -> Unit = {},
    onSubmitMakeoverConfig: (
        sceneId: String,
        roomType: RoomType,
        style: DesignStyle,
        palettes: List<String>,
        atmosphere: DesignAtmosphere,
        furniturePrefs: List<ProductCategory>,
        targetBudgetIdr: Long,
        instructions: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> onRegenerate() },
    onRetryFailedGeneration: (String) -> Unit = {},
    onCancelGeneration: (String) -> Unit = {},
    onDismissError: () -> Unit = {},
    onOpenProductDetail: (String) -> Unit = { onOpenShopping() },
    onAddProductToShoppingList: (String) -> Unit = { onOpenShopping() },
    onTryProductInRoom: (String) -> Unit = { onOpenTryOn(scene.id) },
    onOpenCreditWallet: () -> Unit = {}
) {
    // Fallback deterministic result if activeDesignResult is not explicitly provided
    val resolvedResult = remember(scene, activeDesignResult) {
        activeDesignResult ?: run {
            val fallbackStyle = scene.preferredStyle
                ?: scene.fullMakeovers.firstOrNull()?.style
                ?: DesignStyle.JAPANDI
            val fallbackReq = DesignGenerationRequest(
                requestId = "req_default_${scene.id}",
                idempotencyKey = "idem_default_${scene.id}",
                sceneId = scene.id,
                originalRoomImageUri = scene.originalImage.localUri,
                roomType = scene.roomType,
                selectedStyle = fallbackStyle,
                colorPaletteNames = listOf("Warm Linen", "Natural Oak", "Sage Mist"),
                desiredAtmosphere = DesignAtmosphere.WARM_COZY,
                furniturePreferences = listOf(ProductCategory.SOFA, ProductCategory.COFFEE_TABLE, ProductCategory.LIGHTING),
                targetBudgetIdr = scene.targetBudget
            )
            DeterministicMakeoverGenerator(allCatalogProducts).generateMakeoverResult(
                request = fallbackReq,
                resultId = "res_default_${scene.id}"
            )
        }
    }

    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var localSelectedVariationId by remember(resolvedResult.resultId, resolvedResult.selectedVariationId) {
        mutableStateOf(resolvedResult.selectedVariationId ?: resolvedResult.variations.firstOrNull()?.variationId)
    }

    val activeVariation = resolvedResult.variations.find { it.variationId == localSelectedVariationId }
        ?: resolvedResult.activeVariation

    // Configuration Form State (Part A)
    var configRoomType by remember(scene.id) { mutableStateOf(scene.roomType) }
    var configStyle by remember(scene.id, scene.preferredStyle) {
        mutableStateOf(scene.preferredStyle ?: resolvedResult.selectedStyle)
    }
    val activeStyleMoodboard = remember(configStyle, styleMoodboards) {
        styleMoodboards.find { it.style == configStyle }
            ?: ConsultantRecommendationEngine().getStyleMoodboardProfile(configStyle)
    }
    var configPalettes by remember(configStyle) {
        mutableStateOf<List<String>>(activeStyleMoodboard.colorPaletteNamed.map { it.first })
    }
    var configAtmosphere by remember(scene.id) {
        mutableStateOf(resolvedResult.desiredAtmosphere)
    }
    var configFurnitureCategories by remember(scene.id) {
        mutableStateOf(
            resolvedResult.furniturePreferences.ifEmpty {
                listOf(ProductCategory.SOFA, ProductCategory.COFFEE_TABLE, ProductCategory.LIGHTING)
            }
        )
    }
    var configBudgetText by remember(scene.id) {
        mutableStateOf(scene.targetBudget.toString())
    }
    var configInstructions by remember(scene.id) {
        mutableStateOf("")
    }
    var configValidationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Makeover Ruangan AI",
                subtitle = "${scene.title} • ${scene.roomType.displayName}",
                onBackClick = onBack,
                actionContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (creditAvailable > 0) WarmCamelAccent.copy(alpha = 0.16f) else TerracottaError.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clickable(onClick = onOpenCreditWallet)
                            .testTag("makeover_credit_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (creditAvailable > 0) WarmCamelAccent else TerracottaError,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$creditAvailable Kredit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }
                    }
                }
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Mode Tabs: Hasil & Variasi | Konfigurasi Baru | Riwayat & Favorit
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                val tabs = listOf(
                    "Hasil & Variasi (${resolvedResult.variations.size})",
                    "Konfigurasi Baru",
                    "Riwayat (${designHistory.size.coerceAtLeast(1)})"
                )
                tabs.forEachIndexed { idx, label ->
                    val isSelected = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CharcoalDark else PureWhite,
                        border = BorderStroke(1.dp, if (isSelected) CharcoalDark else OutlineWarm),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTab = idx }
                            .testTag("makeover_mode_tab_$idx")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PureWhite else CharcoalDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Transient Status Notice Banner
            if (statusNotice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ForestGreen.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusNotice,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalDark
                        )
                    }
                }
            }

            // Error / Auto-Refund / Retry Banner
            if (makeoverError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TerracottaError.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TerracottaError.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("makeover_error_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TerracottaError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = makeoverError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalDark,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onDismissError, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (lastFailedRequestId != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onRetryFailedGeneration(lastFailedRequestId) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_retry_makeover")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Coba Ulang (Retry)", fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Processing / Asynchronous Generation Indicator
            if (isGeneratingMakeover || resolvedResult.status == DesignGenerationStatus.PROCESSING) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WarmCamelAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("makeover_processing_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = WarmCamelAccent,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sedang Merender Makeover AI (Simulasi)...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CharcoalDark
                            )
                            Text(
                                text = "Menyusun 3 variasi desain & mencocokkan katalog furnitur",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(
                            onClick = { onCancelGeneration(resolvedResult.requestId) }
                        ) {
                            Text("Batal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaError)
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> MakeoverResultsTabContent(
                    scene = scene,
                    result = resolvedResult,
                    activeVariation = activeVariation,
                    allCatalogProducts = allCatalogProducts,
                    onSelectVariation = { varId ->
                        localSelectedVariationId = varId
                        onSelectVariation(resolvedResult.resultId, varId)
                    },
                    onToggleFavorite = { varId, fav ->
                        onToggleFavoriteVariation(varId, fav)
                    },
                    onOpenTryOn = onOpenTryOn,
                    onOpenShopping = onOpenShopping,
                    onOpenConfigureTab = { selectedTab = 1 },
                    onRegenerate = onRegenerate,
                    onOpenProductDetail = onOpenProductDetail,
                    onAddProductToShoppingList = onAddProductToShoppingList,
                    onTryProductInRoom = onTryProductInRoom
                )

                1 -> MakeoverConfigTabContent(
                    scene = scene,
                    allScenes = allScenes,
                    styleMoodboards = styleMoodboards,
                    selectedRoomType = configRoomType,
                    selectedStyle = configStyle,
                    selectedPalettes = configPalettes,
                    selectedAtmosphere = configAtmosphere,
                    selectedFurnitureCategories = configFurnitureCategories,
                    budgetText = configBudgetText,
                    instructions = configInstructions,
                    creditAvailable = creditAvailable,
                    validationError = configValidationError,
                    onSelectScene = onSelectScene,
                    onRoomTypeChange = { configRoomType = it },
                    onStyleChange = { newStyle ->
                        configStyle = newStyle
                        val profile = styleMoodboards.find { it.style == newStyle }
                            ?: ConsultantRecommendationEngine().getStyleMoodboardProfile(newStyle)
                        configPalettes = profile.colorPaletteNamed.map { it.first }
                    },
                    onTogglePalette = { paletteName ->
                        configPalettes = if (configPalettes.contains(paletteName)) {
                            if (configPalettes.size > 1) configPalettes - paletteName else configPalettes
                        } else {
                            configPalettes + paletteName
                        }
                    },
                    onAtmosphereChange = { configAtmosphere = it },
                    onToggleFurnitureCategory = { cat ->
                        configFurnitureCategories = if (configFurnitureCategories.contains(cat)) {
                            configFurnitureCategories - cat
                        } else {
                            configFurnitureCategories + cat
                        }
                    },
                    onBudgetTextChange = { configBudgetText = it },
                    onInstructionsChange = { configInstructions = it },
                    onOpenCreditWallet = onOpenCreditWallet,
                    onSubmit = {
                        val parsedBudget = configBudgetText.replace(".", "").replace(",", "").trim().toLongOrNull() ?: 0L
                        val testReq = DesignGenerationRequest(
                            requestId = "req_validate",
                            idempotencyKey = "idem_validate",
                            sceneId = scene.id,
                            originalRoomImageUri = scene.originalImage.localUri,
                            roomType = configRoomType,
                            selectedStyle = configStyle,
                            colorPaletteNames = configPalettes,
                            desiredAtmosphere = configAtmosphere,
                            furniturePreferences = configFurnitureCategories,
                            targetBudgetIdr = parsedBudget,
                            additionalInstructions = configInstructions
                        )
                        val validation = testReq.validate()
                        if (!validation.isValid) {
                            configValidationError = validation.errors.joinToString(" ")
                        } else {
                            configValidationError = null
                            selectedTab = 0
                            onSubmitMakeoverConfig(
                                scene.id,
                                configRoomType,
                                configStyle,
                                configPalettes,
                                configAtmosphere,
                                configFurnitureCategories,
                                parsedBudget,
                                configInstructions
                            )
                        }
                    }
                )

                2 -> MakeoverHistoryAndFavoritesTabContent(
                    scene = scene,
                    designHistory = designHistory.ifEmpty { listOf(resolvedResult) },
                    favoriteVariations = favoriteVariations,
                    activeResultId = resolvedResult.resultId,
                    onSelectHistoryResult = { resId ->
                        onSelectHistoryResult(resId)
                        selectedTab = 0
                    },
                    onSelectFavoriteVariation = { variation ->
                        onSelectHistoryResult(variation.resultId)
                        localSelectedVariationId = variation.variationId
                        onSelectVariation(variation.resultId, variation.variationId)
                        selectedTab = 0
                    },
                    onToggleFavorite = onToggleFavoriteVariation
                )
            }
        }
    }
}

@Composable
private fun MakeoverResultsTabContent(
    scene: RoomScene,
    result: DesignGenerationResult,
    activeVariation: DesignVariation?,
    allCatalogProducts: List<Product>,
    onSelectVariation: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onOpenTryOn: (String) -> Unit,
    onOpenShopping: () -> Unit,
    onOpenConfigureTab: () -> Unit,
    onRegenerate: () -> Unit,
    onOpenProductDetail: (String) -> Unit,
    onAddProductToShoppingList: (String) -> Unit,
    onTryProductInRoom: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. Simulated Result Disclosure Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = WarmCamelAccent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, WarmCamelAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("makeover_simulation_disclosure_banner")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = WarmCamelAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeVariation?.simulationDisclosure ?: result.simulationDisclosure,
                    fontSize = 11.sp,
                    color = CharcoalDark,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Interactive Before/After Split Slider (Preserves scene.originalImage.localUri untouched)
        if (activeVariation != null) {
            BeforeAfterSlider(
                beforeImageUrl = scene.originalImage.localUri,
                afterImageUrl = activeVariation.previewImageUrl
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Foto Asli Terjaga (Tidak Ditimpa)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Geser slider untuk membandingkan Before / After",
                    fontSize = 10.sp,
                    color = WarmCamelAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Multiple Design Variations Switcher + Favorite Toggle
        if (result.variations.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pilih Variasi Desain (${result.variations.size} Opsi):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark
                    )
                )

                if (activeVariation != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeVariation.isFavorite) TerracottaError.copy(alpha = 0.14f) else PureWhite,
                        border = BorderStroke(
                            1.dp,
                            if (activeVariation.isFavorite) TerracottaError else OutlineWarm
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onToggleFavorite(activeVariation.variationId, !activeVariation.isFavorite)
                            }
                            .testTag("btn_favorite_active_variation")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (activeVariation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorit",
                                tint = if (activeVariation.isFavorite) TerracottaError else CharcoalDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeVariation.isFavorite) "Tersimpan Favorit" else "Simpan Favorit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(result.variations, key = { it.variationId }) { variation ->
                    val isSelected = variation.variationId == activeVariation?.variationId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CharcoalDark else PureWhite,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) WarmCamelAccent else OutlineWarm
                        ),
                        modifier = Modifier
                            .width(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectVariation(variation.variationId) }
                            .testTag("variation_card_${variation.variationIndex}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Variasi ${variation.variationIndex + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) WarmCamelAccent else TextSecondary
                                )
                                if (variation.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorit",
                                        tint = TerracottaError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = variation.variationTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PureWhite else CharcoalDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Est. Rp " + "%,d".format(variation.estimatedFurnitureBudgetIdr).replace(',', '.'),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) WarmLinen else ForestGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Design Metadata & Concept Summary Card
        val displayStyle = activeVariation?.style ?: result.selectedStyle
        val displayBudget = activeVariation?.estimatedFurnitureBudgetIdr
            ?: scene.fullMakeovers.firstOrNull()?.estimatedBudgetTotalIdr
            ?: 2_500_000L

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = WarmCamelAccent.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Tema: ${displayStyle.displayName}",
                    color = CharcoalDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "Estimasi Total: Rp " + "%,d".format(displayBudget).replace(',', '.'),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    fontSize = 14.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PureWhite,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("design_metadata_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activeVariation?.variationTitle ?: "Konsep Desain:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CharcoalDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeVariation?.designSummary
                        ?: scene.fullMakeovers.firstOrNull()?.designSummary
                        ?: "Transformasi minimalis dengan pengoptimalan pencahayaan dan furnitur fungsional.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Metadata chips: Atmosphere, RoomType, Palettes, Target Budget
                MetadataBulletRow(
                    label = "Suasana & Pencahayaan",
                    value = "${(activeVariation?.atmosphere ?: result.desiredAtmosphere).displayName} (${(activeVariation?.atmosphere ?: result.desiredAtmosphere).lightingHint})"
                )
                MetadataBulletRow(
                    label = "Palet Warna",
                    value = (activeVariation?.colorPaletteNames ?: result.colorPaletteNames).joinToString(", ")
                )
                MetadataBulletRow(
                    label = "Tipe Ruangan & Target Budget",
                    value = "${result.roomType.displayName} • Target Rp ${"%,d".format(result.targetBudgetIdr).replace(',', '.')}"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Catalog-Matched Products from Design (Part D)
        Text(
            text = "Furnitur Terpasang di Desain (Katalog Terverifikasi):",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalDark
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Explicit distinction between catalog-matched products and AI-generated visual elements
        Text(
            text = "Catatan Transparansi: Produk di bawah ini adalah furnitur nyata dari katalog RuangAI yang cocok secara gaya, fungsi, dan anggaran — bukan objek piksel identik dari gambar pratinjau simulasi.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp,
            modifier = Modifier.testTag("catalog_match_distinction_notice")
        )

        Spacer(modifier = Modifier.height(10.dp))

        val matchedItems = activeVariation?.catalogMatchedProducts.orEmpty()
        if (matchedItems.isNotEmpty()) {
            matchedItems.forEach { matched ->
                val catalogProd = allCatalogProducts.find { it.id == matched.productId }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, OutlineWarm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("matched_product_card_${matched.productId}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (catalogProd != null) {
                                AsyncImage(
                                    model = catalogProd.imageUrl,
                                    contentDescription = matched.productTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceWarm)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = WarmCamelAccent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = matched.roleInDesign,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = matched.productTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CharcoalDark
                                )
                                Text(
                                    text = "Rp " + "%,d".format(matched.priceIdr).replace(',', '.') +
                                        (catalogProd?.let { " • ${it.marketplace.displayName}" } ?: ""),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen
                                )
                                Text(
                                    text = matched.compatibilityReason,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onOpenProductDetail(matched.productId) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Detail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                            }

                            OutlinedButton(
                                onClick = { onAddProductToShoppingList(matched.productId) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Belanja", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                            }

                            Button(
                                onClick = { onTryProductInRoom(matched.productId) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text("Coba 2D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Primary & Secondary Actions
        Button(
            onClick = { onOpenTryOn(scene.id) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("results_btn_tryon")
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Coba Ganti Furnitur (Virtual Try-On)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PureWhite)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onOpenConfigureTab,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("results_btn_configure")
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ubah Konfigurasi", color = CharcoalDark, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onRegenerate,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("results_btn_regenerate")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Ulang Variasi Lain", color = CharcoalDark, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MakeoverConfigTabContent(
    scene: RoomScene,
    allScenes: List<RoomScene>,
    styleMoodboards: List<StyleMoodboardProfile>,
    selectedRoomType: RoomType,
    selectedStyle: DesignStyle,
    selectedPalettes: List<String>,
    selectedAtmosphere: DesignAtmosphere,
    selectedFurnitureCategories: List<ProductCategory>,
    budgetText: String,
    instructions: String,
    creditAvailable: Int,
    validationError: String?,
    onSelectScene: (String) -> Unit,
    onRoomTypeChange: (RoomType) -> Unit,
    onStyleChange: (DesignStyle) -> Unit,
    onTogglePalette: (String) -> Unit,
    onAtmosphereChange: (DesignAtmosphere) -> Unit,
    onToggleFurnitureCategory: (ProductCategory) -> Unit,
    onBudgetTextChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onOpenCreditWallet: () -> Unit,
    onSubmit: () -> Unit
) {
    val activeStyleMoodboard = styleMoodboards.find { it.style == selectedStyle }
        ?: ConsultantRecommendationEngine().getStyleMoodboardProfile(selectedStyle)

    val availablePaletteOptions: List<String> = remember(selectedStyle) {
        (activeStyleMoodboard.colorPaletteNamed.map { it.first } + listOf("Warm Linen", "Charcoal Slate", "Terracotta Clay", "Sage Green")).distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Room Project Selector
        if (allScenes.size > 1) {
            ConfigSectionCard(title = "1. Pilih Proyek Ruangan") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allScenes, key = { it.id }) { itemScene ->
                        val isSelected = itemScene.id == scene.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectScene(itemScene.id) },
                            label = { Text(itemScene.title, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 2. Room Type
        ConfigSectionCard(title = "2. Tipe Ruangan") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RoomType.values().toList()) { rt ->
                    FilterChip(
                        selected = rt == selectedRoomType,
                        onClick = { onRoomTypeChange(rt) },
                        label = { Text(rt.displayName, fontSize = 12.sp) }
                    )
                }
            }
        }

        // 3. Interior Style (All 10 Styles)
        ConfigSectionCard(title = "3. Gaya Interior (10 Tema Desain)") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(DesignStyle.values().toList()) { style ->
                    val isSelected = style == selectedStyle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CharcoalDark else SurfaceWarm,
                        border = BorderStroke(1.dp, if (isSelected) WarmCamelAccent else OutlineWarm),
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onStyleChange(style) }
                            .testTag("config_style_${style.name}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = style.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) PureWhite else CharcoalDark
                            )
                            Text(
                                text = style.subtitle,
                                fontSize = 10.sp,
                                color = if (isSelected) WarmLinen else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Color Palette
        ConfigSectionCard(title = "4. Palet Warna Dominan") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availablePaletteOptions) { paletteName ->
                    val isSelected = selectedPalettes.contains(paletteName)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTogglePalette(paletteName) },
                        label = { Text(paletteName, fontSize = 12.sp) }
                    )
                }
            }
        }

        // 5. Desired Atmosphere
        ConfigSectionCard(title = "5. Suasana & Pencahayaan (Atmosphere)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DesignAtmosphere.values().forEach { atmosphere ->
                    val isSelected = atmosphere == selectedAtmosphere
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) WarmCamelAccent.copy(alpha = 0.16f) else SurfaceWarm,
                        border = BorderStroke(1.dp, if (isSelected) WarmCamelAccent else OutlineWarm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAtmosphereChange(atmosphere) }
                            .testTag("config_atmosphere_${atmosphere.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onAtmosphereChange(atmosphere) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(atmosphere.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                                Text(atmosphere.subtitle, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 6. Furniture Category Preferences
        ConfigSectionCard(title = "6. Prioritas Kategori Furnitur") {
            val selectableCategories = remember {
                ProductCategory.values().filter { it != ProductCategory.ALL }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectableCategories) { cat ->
                    FilterChip(
                        selected = selectedFurnitureCategories.contains(cat),
                        onClick = { onToggleFurnitureCategory(cat) },
                        label = { Text(cat.displayName, fontSize = 12.sp) }
                    )
                }
            }
        }

        // 7. Target Budget & Additional Natural-Language Instructions
        ConfigSectionCard(title = "7. Target Anggaran & Instruksi Tambahan") {
            val presetBudgets = listOf(1_500_000L, 3_000_000L, 5_000_000L, 8_000_000L, 12_000_000L)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetBudgets) { preset ->
                    FilterChip(
                        selected = budgetText == preset.toString(),
                        onClick = { onBudgetTextChange(preset.toString()) },
                        label = { Text("Rp ${"%,d".format(preset).replace(',', '.')}", fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = budgetText,
                onValueChange = onBudgetTextChange,
                label = { Text("Target Anggaran (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("config_budget_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = instructions,
                onValueChange = onInstructionsChange,
                label = { Text("Instruksi Tambahan (Opsional)") },
                placeholder = { Text("Contoh: Pertahankan meja dekat jendela, tambahkan sudut baca hangat...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("config_instructions_input")
            )
        }

        if (validationError != null) {
            Text(
                text = validationError,
                color = TerracottaError,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Credit Wallet Gate & Submit Button
        if (creditAvailable < 1) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = TerracottaError.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, TerracottaError),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("makeover_credit_depleted_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Saldo Kredit Habis (0 Kredit)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TerracottaError
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Makeover Ruangan AI membutuhkan 1 Kredit untuk menghasilkan 3 variasi konsep desain.",
                        fontSize = 12.sp,
                        color = CharcoalDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenCreditWallet,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Top Up Kredit Sekarang", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Button(
                onClick = onSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_makeover_config")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Makeover AI (Simulasi • 1 Kredit)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PureWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MakeoverHistoryAndFavoritesTabContent(
    scene: RoomScene,
    designHistory: List<DesignGenerationResult>,
    favoriteVariations: List<DesignVariation>,
    activeResultId: String,
    onSelectHistoryResult: (String) -> Unit,
    onSelectFavoriteVariation: (DesignVariation) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Variasi Favorit Tersimpan (${favoriteVariations.size})",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalDark
            )
        )

        if (favoriteVariations.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, OutlineWarm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Belum ada variasi desain yang ditandai sebagai favorit pada proyek ${scene.title}.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            favoriteVariations.forEach { fav ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, WarmCamelAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectFavoriteVariation(fav) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fav.variationTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text(
                                text = "${fav.style.displayName} • ${fav.atmosphere.displayName} • Rp ${"%,d".format(fav.estimatedFurnitureBudgetIdr).replace(',', '.')}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { onToggleFavorite(fav.variationId, false) }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Hapus Favorit", tint = TerracottaError)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Riwayat Generasi Desain (${designHistory.size})",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalDark
            )
        )

        designHistory.forEach { historyItem ->
            val isCurrent = historyItem.resultId == activeResultId
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                border = BorderStroke(
                    width = if (isCurrent) 1.5.dp else 1.dp,
                    color = if (isCurrent) CharcoalDark else OutlineWarm
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectHistoryResult(historyItem.resultId) }
                    .testTag("history_result_${historyItem.resultId}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Makeover ${historyItem.selectedStyle.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CharcoalDark
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarmCamelAccent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${historyItem.status.displayName} • ${historyItem.variations.size} Variasi",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Suasana: ${historyItem.desiredAtmosphere.displayName} • Palet: ${historyItem.colorPaletteNames.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (historyItem.additionalInstructions.isNotBlank()) {
                        Text(
                            text = "Instruksi: \"${historyItem.additionalInstructions}\"",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, OutlineWarm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CharcoalDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MetadataBulletRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalDark,
            modifier = Modifier.weight(0.58f)
        )
    }
}
