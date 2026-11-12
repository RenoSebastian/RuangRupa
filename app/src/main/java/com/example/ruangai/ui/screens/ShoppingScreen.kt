package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.DesignStyle
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.ProductCategory
import com.example.ruangai.domain.model.ProductSortOption
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    products: List<Product>,
    selectedCategory: ProductCategory,
    selectedStyle: DesignStyle? = null,
    selectedSortOption: ProductSortOption = ProductSortOption.RELEVANCE,
    maxPriceFilter: Long? = null,
    searchQuery: String,
    comparisonProductIds: List<String> = emptyList(),
    onCategorySelect: (ProductCategory) -> Unit,
    onStyleSelect: (DesignStyle?) -> Unit = {},
    onSortOptionSelect: (ProductSortOption) -> Unit = {},
    onMaxPriceSelect: (Long?) -> Unit = {},
    onSearchChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onTryOnClick: (Product) -> Unit,
    onToggleCompare: (Product) -> Unit = {},
    onOpenComparison: () -> Unit = {},
    onClearComparison: () -> Unit = {},
    onOpenCart: () -> Unit,
    cartItemCount: Int,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val priceFilters = listOf(
        null to "Semua Harga",
        500_000L to "< 500rb",
        1_500_000L to "< 1.5 Juta",
        3_000_000L to "< 3 Juta"
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = "Katalog Furnitur AI",
                subtitle = "Produk Riil Terkurasi • Simulasi Katalog",
                cartItemCount = cartItemCount,
                onCartClick = onOpenCart
            )
        },
        bottomBar = {
            // Floating Comparison Bottom Bar (when 1-3 items selected)
            if (comparisonProductIds.isNotEmpty()) {
                Surface(
                    color = CharcoalDark,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = WarmCamelAccent,
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${comparisonProductIds.size}",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Komparasi Furnitur", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${comparisonProductIds.size}/3 produk terpilih", color = SurfaceVariantWarm, fontSize = 10.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onClearComparison) {
                                Text("Batal", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = onOpenComparison,
                                colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_comparison_btn")
                            ) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bandingkan", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + (if (comparisonProductIds.isNotEmpty()) 80.dp else 24.dp),
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Search & Sort Bar Row
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Cari sofa, meja kopi, brand...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus pencarian", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CharcoalDark,
                            unfocusedBorderColor = OutlineWarm,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shopping_search_input")
                    )

                    // Sort Option Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                            modifier = Modifier
                                .height(52.dp)
                                .clickable { showSortMenu = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Urutkan", tint = CharcoalDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (selectedSortOption) {
                                        ProductSortOption.RELEVANCE -> "Relevan"
                                        ProductSortOption.PRICE_LOW_HIGH -> "Termurah"
                                        ProductSortOption.PRICE_HIGH_LOW -> "Termahal"
                                        ProductSortOption.POPULARITY -> "Rating"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalDark
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ProductSortOption.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.displayName, fontSize = 12.sp) },
                                    onClick = {
                                        onSortOptionSelect(opt)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProductCategory.entries) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(cat) },
                            label = { Text(cat.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CharcoalDark,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }

            // Price Range Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priceFilters.forEach { (price, label) ->
                        val isSelected = maxPriceFilter == price
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) WarmCamelAccent.copy(alpha = 0.18f) else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) WarmCamelAccent else OutlineWarm
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMaxPriceSelect(price) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CharcoalDark else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Style Filter Pill
                    DesignStyle.entries.take(4).forEach { style ->
                        val isSelected = selectedStyle == style
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ForestGreen.copy(alpha = 0.15f) else PureWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreen else OutlineWarm
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onStyleSelect(if (isSelected) null else style) }
                        ) {
                            Text(
                                text = "Gaya: ${style.displayName}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ForestGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Catalog Disclaimer Banner (Official Transparency)
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantWarm
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ForestGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Simulasi Katalog",
                                color = ForestGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Katalog terkurasi dari Shopee, Tokopedia & IKEA Indonesia.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Product Grid or Empty State
            if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tidak ada produk yang cocok", fontWeight = FontWeight.Bold, color = CharcoalDark, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Coba atur ulang kata kunci pencarian atau filter harga.", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(products.chunked(2)) { pair ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pair.forEach { prod ->
                            val isCompared = prod.id in comparisonProductIds
                            Box(modifier = Modifier.weight(1f)) {
                                CatalogProductCard(
                                    product = prod,
                                    isCompared = isCompared,
                                    onProductClick = onProductClick,
                                    onTryOnClick = onTryOnClick,
                                    onToggleCompare = onToggleCompare
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
    }
}

@Composable
fun CatalogProductCard(
    product: Product,
    isCompared: Boolean = false,
    onProductClick: (Product) -> Unit,
    onTryOnClick: (Product) -> Unit,
    onToggleCompare: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) }
            .testTag("catalog_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail with Marketplace badge & Compare Checkbox
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantWarm)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Marketplace Badge
                Surface(
                    color = Color(product.marketplace.badgeColor),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.marketplace.displayName,
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Comparison Toggle
                Surface(
                    color = if (isCompared) CharcoalDark else PureWhite.copy(alpha = 0.85f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clickable { onToggleCompare(product) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCompared) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Bandingkan",
                            tint = if (isCompared) WarmCamelAccent else CharcoalDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Discount Tag
                product.getDiscountPercentage()?.let { disc ->
                    Surface(
                        color = TerracottaRed,
                        shape = RoundedCornerShape(topStart = 6.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "-$disc%",
                            color = PureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CharcoalDark,
                maxLines = 1
            )

            Text(
                text = "${product.brand} • ${product.dimensions.toDisplayString()}",
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.getFormattedPrice(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ForestGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Try-On Button
            OutlinedButton(
                onClick = { onTryOnClick(product) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Coba di Ruang", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
            }
        }
    }
}
