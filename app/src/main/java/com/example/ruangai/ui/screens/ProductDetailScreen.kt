package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.AffiliateRedirectInfo
import com.example.ruangai.domain.model.Product
import com.example.ruangai.ui.components.AffiliateAttributionDialog
import com.example.ruangai.ui.components.AffiliateDisclosureBanner
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.FurnitureCutoutImage
import com.example.ui.theme.*

@Composable
fun ProductDetailScreen(
    product: Product,
    relatedProducts: List<Product> = emptyList(),
    alternativeProducts: List<Product> = emptyList(),
    isCompared: Boolean = false,
    sourceSceneId: String? = null,
    sourcePlacementId: String? = null,
    activeAffiliatePreview: AffiliateRedirectInfo? = null,
    onBack: () -> Unit,
    onTryOnInMyRoom: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleCompare: (Product) -> Unit = {},
    onSelectOtherProduct: (Product) -> Unit = {},
    onReplaceInScene: ((String, String, Product) -> Unit)? = null,
    onTriggerAffiliateClick: (Product) -> Unit = {},
    onDismissAffiliateDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddedToast by remember { mutableStateOf(false) }
    var selectedGalleryIndex by remember(product.id) { mutableStateOf(0) } // 0 = Main Photo, 1 = Cutout 2D

    Scaffold(
        topBar = {
            AppHeader(
                title = "Detail Produk",
                subtitle = "${product.brand} • ${product.demoLabel}",
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Secondary CTA: Tambahkan ke Shopping List
                    OutlinedButton(
                        onClick = {
                            onAddToCart(product)
                            showAddedToast = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambahkan ke Shopping List", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    // Primary CTA: Coba di Ruangan Saya
                    Button(
                        onClick = { onTryOnInMyRoom(product) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("detail_try_on_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Coba di Ruangan Saya", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Toast Notice
            if (showAddedToast) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ForestGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "✓ ${product.title} ditambahkan ke Shopping List",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Design-to-Buy Placement Context Banner (if opened from Virtual Try-On placement)
            if (sourceSceneId != null && sourcePlacementId != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarmCamelAccent.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCamelAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Terhubung ke Penempatan Virtual Try-On",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CharcoalDark
                            )
                            Text(
                                text = "Pilih produk alternatif di bawah untuk mengganti furnitur ini tanpa mengubah tata letak furnitur lain di ruangan Anda.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Product Image Gallery (Photo vs Transparent Cutout 2D)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PureWhite),
                contentAlignment = Alignment.Center
            ) {
                if (selectedGalleryIndex == 0) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FurnitureCutoutImage(
                        productId = product.id,
                        productTitle = product.title,
                        categoryName = product.category.displayName,
                        colorHex = product.colorHex,
                        dimensions = product.dimensions,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }

                // Marketplace Badge
                Surface(
                    color = Color(product.marketplace.badgeColor),
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${product.marketplace.displayName} • ${product.demoLabel}",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Compare Toggle Button
                Surface(
                    color = if (isCompared) WarmCamelAccent else PureWhite.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clickable { onToggleCompare(product) }
                        .testTag("detail_compare_toggle_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Bandingkan",
                            tint = if (isCompared) PureWhite else CharcoalDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompared) "Dibandingkan" else "+ Bandingkan",
                            color = if (isCompared) PureWhite else CharcoalDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gallery Mode Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedGalleryIndex == 0,
                    onClick = { selectedGalleryIndex = 0 },
                    label = { Text("Foto Katalog", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedGalleryIndex == 1,
                    onClick = { selectedGalleryIndex = 1 },
                    label = { Text("Cutout Transparan (Virtual Try-On)", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand & Availability Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${product.brand} • ${product.category.displayName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmCamelAccent
                )
                Surface(
                    color = SurfaceVariantWarm,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = product.availabilityStatus.displayLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Title
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price & Discount Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = product.getFormattedPrice(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark,
                        fontSize = 22.sp
                    )
                )

                val origPrice = product.getFormattedOriginalPrice()
                if (origPrice != null) {
                    Text(
                        text = origPrice,
                        fontSize = 13.sp,
                        color = TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Surface(
                        color = ForestGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Hemat ${product.getDiscountPercentage()}%",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compatible Interior Styles Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text("Gaya Cocok:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                product.compatibleStyles.forEach { style ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = style.displayName,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complete Specifications Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spesifikasi Teknis & Material", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                    HorizontalDivider(color = OutlineWarm)
                    SpecRow(label = "ID Produk", value = product.id)
                    SpecRow(label = "Brand / Merek", value = product.brand)
                    SpecRow(label = "Kategori", value = product.category.displayName)
                    SpecRow(label = "Dimensi (P x L x T)", value = product.dimensions.toDisplayString())
                    SpecRow(label = "Material", value = product.material)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Warna", color = TextMuted, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(product.colorHex or -0x1000000))
                                    .border(0.5.dp, OutlineWarm, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = product.color, color = CharcoalDark, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }
                    SpecRow(label = "Penyedia / Marketplace", value = "${product.marketplace.displayName} (${product.storeName})")
                    SpecRow(label = "Status Data", value = "${product.demoLabel} (Diperbarui Nov 2026)")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Deskripsi Produk", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulated Affiliate Outbound Action Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tautan Marketplace & Atribusi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text(
                                text = "Status: ${product.affiliateStatus.displayLabel}",
                                fontSize = 11.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = { onTriggerAffiliateClick(product) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_simulate_affiliate_click")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulasi Klik Afiliasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AffiliateDisclosureBanner()
                }
            }

            // Alternative Products Section (Same Category)
            if (alternativeProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Produk Alternatif Serupa (${product.category.displayName})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CharcoalDark
                )
                Text(
                    text = if (sourceSceneId != null && sourcePlacementId != null)
                        "Ketuk 'Ganti di Ruangan' untuk menukar furnitur pada proyek Anda"
                    else
                        "Pilihan alternatif dengan ukuran dan rentang harga berbeda",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    alternativeProducts.forEach { alt ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .width(190.dp)
                                .clickable { onSelectOtherProduct(alt) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(95.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceVariantWarm),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FurnitureCutoutImage(
                                        productId = alt.id,
                                        productTitle = alt.title,
                                        categoryName = alt.category.displayName,
                                        colorHex = alt.colorHex,
                                        dimensions = alt.dimensions,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(alt.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(alt.dimensions.toDisplayString(), fontSize = 10.sp, color = TextMuted)
                                Text(alt.getFormattedPrice(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreen)
                                Spacer(modifier = Modifier.height(6.dp))

                                if (sourceSceneId != null && sourcePlacementId != null && onReplaceInScene != null) {
                                    Button(
                                        onClick = { onReplaceInScene(sourceSceneId, sourcePlacementId, alt) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .testTag("btn_replace_with_${alt.id}")
                                    ) {
                                        Text("Ganti di Ruangan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onSelectOtherProduct(alt) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp)
                                    ) {
                                        Text("Lihat Alternatif", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Related Products Section (Complementary Categories)
            if (relatedProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Furnitur Pelengkap Gaya ${product.designStyle.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CharcoalDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    relatedProducts.forEach { rel ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .width(175.dp)
                                .clickable { onSelectOtherProduct(rel) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceVariantWarm),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FurnitureCutoutImage(
                                        productId = rel.id,
                                        productTitle = rel.title,
                                        categoryName = rel.category.displayName,
                                        colorHex = rel.colorHex,
                                        dimensions = rel.dimensions,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(rel.category.displayName, fontSize = 10.sp, color = WarmCamelAccent, fontWeight = FontWeight.Bold)
                                Text(rel.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = CharcoalDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(rel.getFormattedPrice(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (activeAffiliatePreview != null) {
        AffiliateAttributionDialog(
            redirectInfo = activeAffiliatePreview,
            onDismiss = onDismissAffiliateDialog
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = CharcoalDark, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}
