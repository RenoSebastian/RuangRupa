package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ruangai.domain.model.Product
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ProductComparisonScreen(
    comparedProducts: List<Product>,
    onRemoveFromComparison: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onTryOnInMyRoom: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBack: () -> Unit,
    onOpenShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppHeader(
                title = "Komparasi Produk",
                subtitle = "Bandingkan Spesifikasi & Harga (${comparedProducts.size}/3)",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (comparedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Produk yang Dibandingkan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pilih hingga 3 produk di katalog untuk membandingkan harga, ukuran, material, dan kecocokan desain.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onOpenShopping,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                    ) {
                        Text("Buka Katalog Belanja", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Demo Disclaimer Banner
                Surface(
                    color = SurfaceVariantWarm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ Simulasi Komparasi: Data spesifikasi & harga diselaraskan langsung dari katalog nyata marketplace.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Side-by-side Horizontal Comparison Matrix
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    comparedProducts.forEach { product ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .width(240.dp)
                                .testTag("comparison_card_${product.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Remove button & Marketplace tag
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color(product.marketplace.badgeColor),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = product.marketplace.displayName,
                                            color = PureWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveFromComparison(product.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus dari komparasi",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Product Image
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceVariantWarm)
                                ) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = product.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CharcoalDark,
                                    maxLines = 2,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = product.getFormattedPrice(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ForestGreen
                                )

                                product.getFormattedOriginalPrice()?.let { orig ->
                                    Text(
                                        text = orig,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = SurfaceVariantWarm)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Specs List
                                ComparisonField(label = "Kategori", value = product.category.displayName)
                                ComparisonField(label = "Dimensi", value = product.dimensions.toDisplayString())
                                ComparisonField(label = "Material", value = product.material)
                                ComparisonField(label = "Warna", value = product.color)
                                ComparisonField(label = "Gaya Desain", value = product.designStyle.displayName)
                                ComparisonField(label = "Kecocokan Gaya", value = "${product.compatibilityScore}%")
                                ComparisonField(label = "Brand / Toko", value = "${product.brand} (${product.storeBadge})")
                                ComparisonField(label = "Rating", value = "★ ${product.rating} (${product.reviewCount})")

                                Spacer(modifier = Modifier.height(14.dp))

                                // Direct Actions
                                Button(
                                    onClick = { onTryOnInMyRoom(product) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Coba di Ruangan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = { onAddToCart(product) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ List Belanja", fontSize = 11.sp, color = CharcoalDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ComparisonField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, color = CharcoalDark, fontWeight = FontWeight.SemiBold, maxLines = 2)
    }
}
