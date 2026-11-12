package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.DesignStyle
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.ui.components.CreditBadge
import com.example.ruangai.ui.components.ProductCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userName: String,
    creditCount: Int,
    scenes: List<RoomScene>,
    products: List<Product>,
    onOpenScanner: () -> Unit,
    onOpenConsultation: () -> Unit,
    onOpenStyleExplorer: () -> Unit,
    onOpenTryOn: (String) -> Unit,
    onOpenDesignResults: (String) -> Unit,
    onOpenProductDetail: (Product) -> Unit,
    onOpenCreditWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmLinen),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Top Greeting Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Halo, $userName 👋",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp
                        )
                    )
                    Text(
                        text = "Wujudkan ruang impian Anda hari ini",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                CreditBadge(
                    creditCount = creditCount,
                    onClick = onOpenCreditWallet
                )
            }
        }

        // 2. Hero Card: "Transform your room with AI"
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("home_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    // Hero Image
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=80",
                        contentDescription = "Ruang Impian",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CharcoalDark.copy(alpha = 0.9f)
                                    ),
                                    startY = 50f
                                )
                            )
                    )

                    // Content Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = WarmCamelAccent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "AI Interior & Visual Commerce",
                                color = PureWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Transformasi Ruangan Anda dengan AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                lineHeight = 26.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual Action Buttons: Makeover vs Try-On
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onOpenScanner,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_cta_makeover")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Makeover Ruangan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }

                            Button(
                                onClick = { onOpenTryOn(scenes.firstOrNull()?.id ?: "demo_scene_kost_01") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_cta_tryon")
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coba Furnitur 2D", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Shortcuts
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Shortcut 1: Konsultasi AI
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenConsultation)
                        .testTag("home_shortcut_consultation")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Konsultasi AI", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text("Tanya Desainer", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                // Shortcut 2: Eksplor Gaya
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenStyleExplorer)
                        .testTag("home_shortcut_styles")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ForestGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Katalog Gaya", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text("10 Tema Desain", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // 4. Proyek Ruangan Terbaru (Demo Scenes)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Proyek Ruangan Terbaru",
                actionLabel = "Pindai Baru",
                onAction = onOpenScanner
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(scenes) { scene ->
                    RoomProjectCard(
                        scene = scene,
                        onOpenTryOn = { onOpenTryOn(scene.id) },
                        onOpenResults = { onOpenDesignResults(scene.id) }
                    )
                }
            }
        }

        // 5. Gaya Interior Populer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Gaya Interior Populer",
                actionLabel = "Lihat Semua (10)",
                onAction = onOpenStyleExplorer
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val popularStyles = listOf(DesignStyle.JAPANDI, DesignStyle.SCANDINAVIAN, DesignStyle.MINIMALIST, DesignStyle.TROPICAL, DesignStyle.RETRO_80S)
                items(popularStyles) { style ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenStyleExplorer)
                    ) {
                        Column {
                            AsyncImage(
                                model = style.imageUrl,
                                contentDescription = style.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(style.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                                Text(style.subtitle, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // 6. Rekomendasi Furnitur Terverifikasi
        item {
            Spacer(modifier = Modifier.height(28.dp))
            SectionHeader(
                title = "Rekomendasi Furnitur",
                actionLabel = "Katalog Lengkap",
                onAction = { /* Navigate to shopping tab */ }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(products.take(4).chunked(2)) { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                pair.forEach { prod ->
                    Box(modifier = Modifier.weight(1f)) {
                        ProductCard(
                            product = prod,
                            onProductClick = onOpenProductDetail,
                            onTryOnClick = { onOpenTryOn(scenes.firstOrNull()?.id ?: "demo_scene_kost_01") }
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalDark,
                fontSize = 17.sp,
                letterSpacing = (-0.3).sp
            )
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmCamelAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
fun RoomProjectCard(
    scene: RoomScene,
    onOpenTryOn: () -> Unit,
    onOpenResults: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenResults)
            .testTag("room_project_card_${scene.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = scene.originalImage.localUri,
                    contentDescription = scene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    color = CharcoalDark.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = scene.roomType.displayName,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = ForestGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${scene.placements.size} Furnitur Terpasang",
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = scene.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Batas Budget: Rp " + "%,d".format(scene.targetBudget).replace(',', '.'),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenResults,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hasil Desain", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                    }

                    Button(
                        onClick = onOpenTryOn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Try-On 2D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }
    }
}
