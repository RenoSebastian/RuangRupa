package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.*
import com.example.ruangai.ui.components.AffiliateDisclosureBanner
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.ProductCard
import com.example.ui.theme.*

@Composable
fun ShoppingScreen(
    products: List<Product>,
    selectedCategory: ProductCategory,
    searchQuery: String,
    selectedStyle: DesignStyle? = null,
    selectedPriceRange: PriceRangeFilter = PriceRangeFilter.ALL,
    selectedSortOption: ProductSortOption = ProductSortOption.RELEVANCE,
    comparedProductIds: List<String> = emptyList(),
    activeSceneTitle: String? = null,
    onCategorySelect: (ProductCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onStyleSelect: (DesignStyle?) -> Unit = {},
    onPriceRangeSelect: (PriceRangeFilter) -> Unit = {},
    onSortSelect: (ProductSortOption) -> Unit = {},
    onResetFilters: () -> Unit = {},
    onToggleCompare: (Product) -> Unit = {},
    onOpenComparison: () -> Unit = {},
    onClearComparison: () -> Unit = {},
    onProductClick: (Product) -> Unit,
    onTryOnClick: (Product) -> Unit,
    onOpenCart: () -> Unit,
    cartItemCount: Int,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Katalog Furnitur AI",
                subtitle = if (activeSceneTitle != null) "Proyek Aktif: $activeSceneTitle" else "Simulasi Katalog Terstruktur",
                cartItemCount = cartItemCount,
                onCartClick = onOpenCart
            )
        },
        bottomBar = {
            if (comparedProductIds.isNotEmpty()) {
                Surface(
                    color = CharcoalDark,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comparison_floating_bar")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${comparedProductIds.size}/3 Produk Dipilih untuk Komparasi",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Bandingkan harga, dimensi, material & gaya",
                                color = WarmCamelAccent,
                                fontSize = 10.sp
                            )
                        }

                        TextButton(onClick = onClearComparison) {
                            Text("Reset", color = PureWhite.copy(alpha = 0.8f), fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenComparison,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmCamelAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_open_comparison")
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bandingkan", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                bottom = innerPadding.calculateBottomPadding() + 90.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Search Bar + Sort Button
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Cari sofa, meja kopi, rotan, nakas...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Bersihkan", tint = TextMuted)
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

                    // Sort Dropdown Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                            modifier = Modifier
                                .height(54.dp)
                                .clickable { showSortMenu = true }
                                .testTag("shopping_sort_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Urutkan", tint = CharcoalDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedSortOption.displayName.substringBefore(":"),
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
                            ProductSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (option == selectedSortOption) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        onSortSelect(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Category Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProductCategory.entries) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) CharcoalDark else PureWhite,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onCategorySelect(cat) }
                                .testTag("category_chip_${cat.name}")
                        ) {
                            Text(
                                text = cat.displayName,
                                color = if (isSelected) PureWhite else CharcoalDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. Interior Style Filter Chips + Price Range Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Style Filter Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStyle == null,
                                onClick = { onStyleSelect(null) },
                                label = { Text("Semua Gaya", fontSize = 11.sp) }
                            )
                        }
                        items(DesignStyle.entries) { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = {
                                    onStyleSelect(if (selectedStyle == style) null else style)
                                },
                                label = { Text(style.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Price Range Filter Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PriceRangeFilter.entries) { range ->
                            val isSelected = selectedPriceRange == range
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) WarmCamelAccent else PureWhite,
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onPriceRangeSelect(range) }
                                    .testTag("price_chip_${range.name}")
                            ) {
                                Text(
                                    text = range.label,
                                    color = if (isSelected) PureWhite else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Affiliate & Simulated Catalog Disclosure Banner + Result Count
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AffiliateDisclosureBanner()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Menampilkan ${products.size} produk furnitur",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalDark
                        )

                        if (selectedCategory != ProductCategory.ALL || selectedStyle != null || selectedPriceRange != PriceRangeFilter.ALL || searchQuery.isNotBlank()) {
                            TextButton(
                                onClick = onResetFilters,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Atur Ulang Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmCamelAccent)
                            }
                        }
                    }
                }
            }

            // 5. Empty State or 2-Column Products Grid
            if (products.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = WarmCamelAccent,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Produk Tidak Ditemukan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = CharcoalDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tidak ada furnitur yang cocok dengan kombinasi filter pencarian Anda saat ini.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onResetFilters,
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Atur Ulang Semua Filter", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(products.chunked(2)) { pair ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pair.forEach { prod ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = prod,
                                    onProductClick = onProductClick,
                                    onTryOnClick = onTryOnClick,
                                    isCompared = comparedProductIds.contains(prod.id),
                                    onCompareToggle = onToggleCompare
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
