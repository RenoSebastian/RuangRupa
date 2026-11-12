package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.AffiliateRedirectInfo
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.domain.model.ShoppingCartItem
import com.example.ruangai.ui.components.AffiliateAttributionDialog
import com.example.ruangai.ui.components.AffiliateDisclosureBanner
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ShoppingListScreen(
    items: List<ShoppingCartItem>,
    scenes: List<RoomScene> = emptyList(),
    totalSelectedPrice: Long,
    targetBudget: Long,
    activeAffiliatePreview: AffiliateRedirectInfo? = null,
    onToggleItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit = { onUpdateQuantity(it, 0) },
    onProductClick: (Product) -> Unit = {},
    onTriggerAffiliateClick: (Product, String?, String?) -> Unit = { _, _, _ -> },
    onDismissAffiliateDialog: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedByScene = items.groupBy { it.associatedSceneId ?: "general" }
    val isGlobalOverBudget = targetBudget > 0 && totalSelectedPrice > targetBudget
    val globalOverrunIdr = (totalSelectedPrice - targetBudget).coerceAtLeast(0L)
    val globalRemainingIdr = (targetBudget - totalSelectedPrice).coerceAtLeast(0L)
    val budgetPercentage = if (targetBudget > 0) {
        (totalSelectedPrice.toFloat() / targetBudget.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Scaffold(
        topBar = {
            AppHeader(
                title = "Shopping List & Budget",
                subtitle = "${items.sumOf { it.quantity }} Unit Furnitur • Simulasi Katalog",
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Estimasi Terpilih", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "Rp " + "%,d".format(totalSelectedPrice).replace(',', '.'),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGlobalOverBudget) TerracottaRed else CharcoalDark
                            )
                        }

                        Button(
                            onClick = {
                                val firstSelected = items.firstOrNull { it.isSelected } ?: items.firstOrNull()
                                if (firstSelected != null) {
                                    onTriggerAffiliateClick(
                                        firstSelected.product,
                                        firstSelected.associatedSceneId,
                                        firstSelected.associatedPlacementId
                                    )
                                }
                            },
                            enabled = items.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                            modifier = Modifier.testTag("shopping_checkout_btn")
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulasi Tautan Afiliasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Overall Target Room Budget Comparison Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_summary_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Budget Proyek Aktif", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Text(
                                text = "Rp " + "%,d".format(targetBudget).replace(',', '.'),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { budgetPercentage },
                            color = if (isGlobalOverBudget) TerracottaRed else if (budgetPercentage > 0.85f) WarmCamelAccent else ForestGreen,
                            trackColor = SurfaceVariantWarm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Terpakai: ${if (targetBudget > 0) ((totalSelectedPrice * 100) / targetBudget) else 0}%",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            if (isGlobalOverBudget) {
                                Text(
                                    text = "Melebihi Budget: +Rp " + "%,d".format(globalOverrunIdr).replace(',', '.'),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaRed,
                                    modifier = Modifier.testTag("budget_overrun_label")
                                )
                            } else {
                                Text(
                                    text = "Sisa Budget: Rp " + "%,d".format(globalRemainingIdr).replace(',', '.'),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    modifier = Modifier.testTag("budget_remaining_label")
                                )
                            }
                        }
                    }
                }
            }

            // 2. Affiliate Disclosure Banner
            item {
                AffiliateDisclosureBanner()
            }

            // 3. Empty State or Room-Grouped Items List
            if (items.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            Text("Shopping List Masih Kosong", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tambahkan furnitur dari Katalog Produk atau langsung dari penempatan Virtual Try-On ruangan Anda.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                groupedByScene.forEach { (sceneKey, groupItems) ->
                    val matchedScene = scenes.find { it.id == sceneKey }
                    val groupTitle = matchedScene?.title ?: "Proyek Ruangan / Belanja Umum"
                    val groupTargetBudget = matchedScene?.targetBudget ?: targetBudget
                    val groupSelectedSubtotal = groupItems.filter { it.isSelected }.sumOf { it.subtotalIdr }
                    val groupOverrun = (groupSelectedSubtotal - groupTargetBudget).coerceAtLeast(0L)

                    item(key = "header_$sceneKey") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantWarm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = groupTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = CharcoalDark
                                    )
                                    Text(
                                        text = "${groupItems.size} jenis furnitur • Target: Rp ${"%,d".format(groupTargetBudget).replace(',', '.')}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Subtotal: Rp " + "%,d".format(groupSelectedSubtotal).replace(',', '.'),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (groupOverrun > 0) TerracottaRed else ForestGreen
                                    )
                                    if (groupOverrun > 0) {
                                        Text(
                                            text = "Over +Rp ${"%,d".format(groupOverrun).replace(',', '.')}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(groupItems, key = { it.itemId }) { item ->
                        val prod = item.product
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_item_${item.itemId}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.isSelected,
                                        onCheckedChange = { onToggleItem(item.itemId) },
                                        colors = CheckboxDefaults.colors(checkedColor = CharcoalDark)
                                    )

                                    AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = prod.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceVariantWarm)
                                            .clickable { onProductClick(prod) }
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onProductClick(prod) }
                                    ) {
                                        Text(
                                            text = prod.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CharcoalDark,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${prod.marketplace.displayName} • ${prod.dimensions.toDisplayString()}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "${prod.getFormattedPrice()} / unit",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "Subtotal: Rp " + "%,d".format(item.subtotalIdr).replace(',', '.'),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ForestGreen
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveItem(item.itemId) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TerracottaRed, modifier = Modifier.size(18.dp))
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceVariantWarm)

                                // Quantity & Affiliate Attribution Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Jumlah:", fontSize = 11.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = { onUpdateQuantity(item.itemId, item.quantity - 1) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("qty_minus_${prod.id}")
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        OutlinedButton(
                                            onClick = { onUpdateQuantity(item.itemId, item.quantity + 1) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("qty_plus_${prod.id}")
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            onTriggerAffiliateClick(
                                                prod,
                                                item.associatedSceneId,
                                                item.associatedPlacementId
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("cart_affiliate_btn_${prod.id}")
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cek Tautan Afiliasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmCamelAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeAffiliatePreview != null) {
        AffiliateAttributionDialog(
            redirectInfo = activeAffiliatePreview,
            onDismiss = onDismissAffiliateDialog
        )
    }
}
