package com.example.ruangai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.data.repository.mock.ConsultantRecommendationEngine
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.DesignStyle
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.domain.model.StyleMoodboardProfile
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.DesignStyleCard
import com.example.ui.theme.*

@Composable
fun StyleExplorerScreen(
    onSelectStyle: (DesignStyle) -> Unit,
    activeScene: RoomScene? = null,
    styleMoodboards: List<StyleMoodboardProfile> = remember {
        ConsultantRecommendationEngine().getAllStyleMoodboardProfiles()
    },
    allProducts: List<Product> = remember { MockData.products },
    onApplyStyleToScene: ((DesignStyle) -> Unit)? = null,
    onDiscussStyleWithAi: ((DesignStyle) -> Unit)? = null,
    onViewProductDetail: ((Product) -> Unit)? = null,
    onTryProductInRoom: ((Product) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activePreferredStyle = activeScene?.preferredStyle
    var expandedStyle by remember(activePreferredStyle) {
        mutableStateOf(activePreferredStyle ?: DesignStyle.JAPANDI)
    }
    var styleAppliedFeedback by remember { mutableStateOf<DesignStyle?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Style Explorer & Moodboard",
                subtitle = activeScene?.let { "Proyek Aktif: ${it.title}" }
                    ?: "10 Gaya Interior dengan Palet, Material & Furnitur",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier
            .fillMaxSize()
            .testTag("style_explorer_screen")
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp,
                start = 20.dp,
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (activeScene != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, WarmCamelAccent.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_scene_style_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Gaya Ruangan Saat Ini (${activeScene.title})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WarmCamelAccent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = activeScene.preferredStyle?.displayName ?: "Japandi (Default)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = WarmCamelAccent,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("active_preferred_style_badge")
                                    )
                                }
                            }
                            Text(
                                text = "Pilih gaya di bawah untuk melihat moodboard material, palet warna, produk katalog yang cocok, dan menyimpannya sebagai preferensi ruangan Anda.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                            if (styleAppliedFeedback != null) {
                                Text(
                                    text = "✓ Gaya ${styleAppliedFeedback?.displayName} berhasil disimpan untuk ${activeScene.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SageGreenSuccess,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("style_applied_feedback")
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Eksplorasi 10 gaya interior kurasi lengkap dengan palet warna, material, pencahayaan, dan rekomendasi furnitur katalog:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            items(styleMoodboards, key = { it.style.name }) { moodboard ->
                val style = moodboard.style
                val isExpanded = expandedStyle == style
                val isAppliedToScene = activeScene?.preferredStyle == style
                val matchingProducts = remember(style, allProducts) {
                    val byId = moodboard.recommendedProductIds.mapNotNull { id ->
                        allProducts.find { it.id == id }
                    }
                    byId.ifEmpty {
                        allProducts.filter {
                            it.designStyle == style || it.compatibleStyles.contains(style)
                        }.take(3)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("style_moodboard_section_${style.name}")
                ) {
                    DesignStyleCard(
                        style = style,
                        onClick = { clickedStyle ->
                            expandedStyle = clickedStyle
                            if (onApplyStyleToScene == null) {
                                onSelectStyle(clickedStyle)
                            }
                        }
                    )

                    Surface(
                        color = if (isExpanded) CharcoalDark else PureWhite,
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                        border = BorderStroke(1.dp, OutlineWarm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clickable {
                                expandedStyle = style
                            }
                            .testTag("toggle_moodboard_${style.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = if (isExpanded) WarmCamelAccent else CharcoalDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isExpanded) {
                                        "Moodboard Aktif • ${style.subtitle}"
                                    } else {
                                        "Lihat Moodboard, Material & Furnitur (${matchingProducts.size} Produk)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isExpanded) PureWhite else CharcoalDark,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = if (isExpanded) PureWhite else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        MoodboardDetailPanel(
                            moodboard = moodboard,
                            matchingProducts = matchingProducts,
                            isAppliedToScene = isAppliedToScene,
                            onApplyStyle = {
                                styleAppliedFeedback = style
                                if (onApplyStyleToScene != null) {
                                    onApplyStyleToScene(style)
                                } else {
                                    onSelectStyle(style)
                                }
                            },
                            onDiscussWithAi = {
                                if (onDiscussStyleWithAi != null) {
                                    onDiscussStyleWithAi(style)
                                } else {
                                    onSelectStyle(style)
                                }
                            },
                            onViewProductDetail = onViewProductDetail,
                            onTryProductInRoom = onTryProductInRoom
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodboardDetailPanel(
    moodboard: StyleMoodboardProfile,
    matchingProducts: List<Product>,
    isAppliedToScene: Boolean,
    onApplyStyle: () -> Unit,
    onDiscussWithAi: () -> Unit,
    onViewProductDetail: ((Product) -> Unit)?,
    onTryProductInRoom: ((Product) -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, WarmCamelAccent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .testTag("moodboard_detail_${moodboard.style.name}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Palet Warna Kurasi",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                moodboard.colorPaletteNamed.forEach { (swatchName, colorHex) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(SurfaceVariantWarm, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(1.dp, OutlineWarm, CircleShape)
                        )
                        Text(
                            text = swatchName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CharcoalDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = "Rekomendasi Material Utama",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                moodboard.materialSuggestions.forEach { material ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmCamelAccent.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = material,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CharcoalDark,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceVariantWarm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Karakter Furnitur:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                    )
                    Text(
                        text = moodboard.furnitureCharacteristics.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pencahayaan & Tata Ruang:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                    )
                    Text(
                        text = "${moodboard.lightingCharacteristics.joinToString(" • ")} • ${moodboard.spaceUtilizationTip}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (matchingProducts.isNotEmpty()) {
                Text(
                    text = "Produk Katalog Sesuai Gaya ${moodboard.style.displayName}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    matchingProducts.forEach { product ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmLinen,
                            border = BorderStroke(1.dp, OutlineWarm),
                            modifier = Modifier
                                .width(205.dp)
                                .clickable { onViewProductDetail?.invoke(product) }
                                .testTag("moodboard_product_${product.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = product.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = product.getFormattedPrice(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarmCamelAccent
                                    )
                                )
                                Text(
                                    text = "${product.dimensions.toDisplayString()} • ${product.material}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (onTryProductInRoom != null) {
                                    TextButton(
                                        onClick = { onTryProductInRoom(product) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ViewInAr,
                                            contentDescription = null,
                                            tint = WarmCamelAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Coba di Ruangan",
                                            fontSize = 11.sp,
                                            color = WarmCamelAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApplyStyle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAppliedToScene) SageGreenSuccess else WarmCamelAccent,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_style_btn_${moodboard.style.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAppliedToScene) "Gaya Aktif" else "Terapkan Gaya Ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDiscussWithAi,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CharcoalDark.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discuss_style_ai_btn_${moodboard.style.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CharcoalDark,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Konsultasi AI",
                        fontSize = 12.sp,
                        color = CharcoalDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
