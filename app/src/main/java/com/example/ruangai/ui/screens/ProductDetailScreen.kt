package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.AffiliateRedirectInfo
import com.example.ruangai.domain.model.Product
import com.example.ruangai.ui.components.AffiliateDisclosureModal
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ProductDetailScreen(
    product: Product,
    relatedProducts: List<Product> = emptyList(),
    isCompared: Boolean = false,
    affiliateInfo: AffiliateRedirectInfo? = null,
    onBack: () -> Unit,
    onTryOnInMyRoom: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleCompare: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onAffiliateClick: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedImageIndex by remember { mutableStateOf(0) }
    var showAddedNotice by remember { mutableStateOf(false) }
    var showAffiliateModal by remember { mutableStateOf(false) }

    val allImages = remember(product) {
        (listOf(product.imageUrl) + product.galleryImages).distinct()
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Detail Produk",
                subtitle = "${product.brand} • ${product.marketplace.displayName}",
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save to Shopping List
                    OutlinedButton(
                        onClick = {
                            onAddToCart(product)
                            showAddedNotice = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ List", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Direct Try-On In Room (Design-to-Buy Differentiator)
                    Button(
                        onClick = { onTryOnInMyRoom(product) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("detail_try_on_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Coba di Ruangan", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            // Added to Cart Notice
            if (showAddedNotice) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ForestGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "✓ Ditambahkan ke Daftar Belanja Ruang!",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Hero Image Gallery Viewer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PureWhite)
            ) {
                AsyncImage(
                    model = allImages.getOrElse(selectedImageIndex) { product.imageUrl },
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                // Marketplace Tag
                Surface(
                    color = Color(product.marketplace.badgeColor),
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${product.marketplace.displayName} • ${product.storeBadge}",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Comparison Action Button
                Surface(
                    color = if (isCompared) CharcoalDark else PureWhite.copy(alpha = 0.9f),
                    shape = CircleShape,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clickable { onToggleCompare(product) }
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = if (isCompared) Icons.Default.Check else Icons.Default.CompareArrows,
                            contentDescription = "Bandingkan",
                            tint = if (isCompared) WarmCamelAccent else CharcoalDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Thumbnail Selector Row
            if (allImages.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allImages.forEachIndexed { idx, url ->
                        val isSelected = idx == selectedImageIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PureWhite,
                            modifier = Modifier
                                .size(50.dp)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, WarmCamelAccent, RoundedCornerShape(8.dp))
                                    else Modifier.border(1.dp, OutlineWarm, RoundedCornerShape(8.dp))
                                )
                                .clickable { selectedImageIndex = idx }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title, Brand & Price
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark,
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Brand: ${product.brand} • Kategori: ${product.category.displayName}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Row with Discount
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = product.getFormattedPrice(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 22.sp
                    )
                )

                product.getFormattedOriginalPrice()?.let { orig ->
                    Text(
                        text = orig,
                        fontSize = 13.sp,
                        color = TextMuted,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                }

                product.getDiscountPercentage()?.let { disc ->
                    Surface(
                        color = TerracottaRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "-$disc%",
                            color = TerracottaRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating, Review, & Style Compatibility Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kecocokan ${product.designStyle.displayName} ${product.compatibilityScore}%",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantWarm
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating} (${product.reviewCount} ulasan)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct Affiliate Marketplace Outbound Action
            Button(
                onClick = {
                    onAffiliateClick(product)
                    showAffiliateModal = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(product.marketplace.badgeColor)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_affiliate_buy_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Beli di ${product.marketplace.displayName} (${product.storeName})",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specifications Matrix
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Spesifikasi & Ukuran Riil", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                    HorizontalDivider(color = SurfaceVariantWarm)
                    SpecRow(label = "Dimensi", value = product.dimensions.toDisplayString())
                    SpecRow(label = "Material", value = product.material)
                    SpecRow(label = "Warna", value = product.color)
                    SpecRow(label = "Gaya Desain", value = product.designStyle.displayName)
                    SpecRow(label = "Brand / Produsen", value = product.brand)
                    SpecRow(label = "Toko Penjual", value = "${product.storeName} (${product.storeBadge})")
                    SpecRow(label = "Status Stok", value = if (product.isAvailable) "Tersedia (Ready)" else "Habis")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description Box
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

            // Related & Alternative Products Section
            if (relatedProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Alternatif Furnitur Serupa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(relatedProducts) { rel ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite,
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { onProductClick(rel) }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = rel.imageUrl,
                                    contentDescription = rel.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantWarm)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(rel.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CharcoalDark, maxLines = 1)
                                Text(rel.getFormattedPrice(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ForestGreen)
                            }
                        }
                    }
                }
            }

            // Affiliate Disclaimer Notice
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceVariantWarm
            ) {
                Text(
                    text = "ℹ️ Simulasi Afiliasi: RuangAI dapat memperoleh komisi dari pembelian yang dilakukan melalui tautan afiliasi untuk mendukung platform tanpa biaya tambahan bagi pengguna.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    // Affiliate Attribution Disclosure Modal
    if (showAffiliateModal && affiliateInfo != null) {
        AffiliateDisclosureModal(
            product = product,
            affiliateInfo = affiliateInfo,
            onConfirmRedirect = {
                showAffiliateModal = false
            },
            onDismiss = {
                showAffiliateModal = false
            }
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
