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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.Product
import com.example.ruangai.ui.components.AffiliateDisclosureBanner
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.FurnitureCutoutImage
import com.example.ui.theme.*

@Composable
fun ProductComparisonScreen(
    comparedProducts: List<Product>,
    onBack: () -> Unit,
    onRemoveFromComparison: (String) -> Unit,
    onClearAll: () -> Unit,
    onProductClick: (Product) -> Unit,
    onTryOnProduct: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowestPriceIdr = comparedProducts.minOfOrNull { it.priceIdr }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Komparasi Furnitur",
                subtitle = "${comparedProducts.size}/3 Produk • Simulasi Katalog",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Explicit Demo Disclaimer Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulasi Katalog — Perbandingan Spesifikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CharcoalDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Seluruh spesifikasi dimensi, harga IDR, dan material di bawah ini merupakan data sampel terstruktur untuk membantu pengambilan keputusan desain ruangan Anda.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    if (comparedProducts.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Reset Semua", color = TerracottaRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (comparedProducts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = WarmCamelAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum Ada Produk untuk Dibandingkan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Pilih hingga 3 produk dari Katalog Furnitur untuk membandingkan ukuran, material, dan harga.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                        ) {
                            Text("Kembali ke Katalog", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Horizontal Scrollable Side-by-Side Comparison Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    comparedProducts.forEach { prod ->
                        val isLowestPrice = comparedProducts.size > 1 && prod.priceIdr == lowestPriceIdr
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PureWhite,
                            tonalElevation = 2.dp,
                            border = if (isLowestPrice) androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreen) else null,
                            modifier = Modifier
                                .width(225.dp)
                                .testTag("compare_column_${prod.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Top Badge + Remove Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = if (isLowestPrice) ForestGreen else CharcoalDark,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isLowestPrice) "Harga Terbaik" else prod.demoLabel,
                                            color = PureWhite,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveFromComparison(prod.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Cutout & Image Preview Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariantWarm)
                                        .clickable { onProductClick(prod) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    FurnitureCutoutImage(
                                        productId = prod.id,
                                        productTitle = prod.title,
                                        categoryName = prod.category.displayName,
                                        colorHex = prod.colorHex,
                                        dimensions = prod.dimensions,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Title
                                Text(
                                    text = prod.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CharcoalDark,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.height(38.dp)
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineWarm)

                                // Criteria 1: Price
                                ComparisonAttrBlock(
                                    label = "Harga (IDR)",
                                    value = prod.getFormattedPrice(),
                                    highlight = isLowestPrice
                                )

                                // Criteria 2: Dimensions
                                ComparisonAttrBlock(
                                    label = "Dimensi (P x L x T)",
                                    value = prod.dimensions.toDisplayString()
                                )

                                // Criteria 3: Material
                                ComparisonAttrBlock(
                                    label = "Material Utama",
                                    value = prod.material
                                )

                                // Criteria 4: Color
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Warna", fontSize = 10.sp, color = TextMuted)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(prod.colorHex or -0x1000000))
                                                    .border(0.5.dp, OutlineWarm, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(prod.color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalDark)
                                        }
                                    }
                                }

                                // Criteria 5: Product Category & Brand
                                ComparisonAttrBlock(
                                    label = "Kategori & Brand",
                                    value = "${prod.category.displayName} • ${prod.brand}"
                                )

                                // Criteria 6: Interior Style Compatibility
                                ComparisonAttrBlock(
                                    label = "Kompatibilitas Gaya",
                                    value = prod.compatibleStyles.joinToString(", ") { it.displayName }
                                )

                                // Criteria 7: Marketplace / Provider & Status
                                ComparisonAttrBlock(
                                    label = "Marketplace & Status",
                                    value = "${prod.marketplace.displayName} (${prod.availabilityStatus.displayLabel})"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Actions
                                Button(
                                    onClick = { onTryOnProduct(prod) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Coba di Ruangan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = { onAddToCart(prod) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Shopping List", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                }
                            }
                        }
                    }
                }
            }

            AffiliateDisclosureBanner()
        }
    }
}

@Composable
private fun ComparisonAttrBlock(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) ForestGreen else CharcoalDark,
            lineHeight = 15.sp
        )
    }
}
