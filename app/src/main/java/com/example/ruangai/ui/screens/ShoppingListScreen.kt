package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.domain.model.ShoppingCartItem
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ShoppingListScreen(
    items: List<ShoppingCartItem>,
    totalSelectedPrice: Long,
    targetBudget: Long,
    activeScene: RoomScene? = null,
    onToggleItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onBack: () -> Unit,
    onOpenShopping: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCheckoutModal by remember { mutableStateOf(false) }

    val selectedItems = items.filter { it.isSelected }
    val budgetOverrun = totalSelectedPrice > targetBudget
    val budgetRemaining = maxOf(0L, targetBudget - totalSelectedPrice)
    val overrunAmount = if (budgetOverrun) totalSelectedPrice - targetBudget else 0L

    val budgetPercentage = if (targetBudget > 0) {
        (totalSelectedPrice.toFloat() / targetBudget.toFloat()).coerceAtLeast(0f)
    } else 0f

    // Group items by associated project/scene
    val groupedItems = items.groupBy { it.associatedSceneId ?: "Umum / Belum Ditentukan" }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Daftar Belanja Ruang",
                subtitle = "Total Estimasi Furnitur Terpilih (${selectedItems.size} Produk)",
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
                            Text("Total Terpilih (${selectedItems.size} item)", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "Rp " + "%,d".format(totalSelectedPrice).replace(',', '.'),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (budgetOverrun) TerracottaRed else ForestGreen
                            )
                        }

                        Button(
                            onClick = { showCheckoutModal = true },
                            enabled = selectedItems.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("shopping_checkout_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beli Semua (Simulasi)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Budget Management & Progress Card
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeScene != null) "Budget: ${activeScene.title}" else "Target Budget Proyek",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalDark
                                )
                            }
                            Text(
                                text = "Rp " + "%,d".format(targetBudget).replace(',', '.'),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Meter
                        LinearProgressIndicator(
                            progress = { budgetPercentage.coerceIn(0f, 1f) },
                            color = when {
                                budgetOverrun -> TerracottaRed
                                budgetPercentage > 0.75f -> WarmCamelAccent
                                else -> ForestGreen
                            },
                            trackColor = SurfaceVariantWarm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Terpakai: ${(budgetPercentage * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            if (budgetOverrun) {
                                Text(
                                    text = "Melebihi: +Rp " + "%,d".format(overrunAmount).replace(',', '.'),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaRed
                                )
                            } else {
                                Text(
                                    text = "Sisa: Rp " + "%,d".format(budgetRemaining).replace(',', '.'),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }
                }
            }

            // Items List Grouped by Room Scene
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Daftar Belanja Masih Kosong", fontWeight = FontWeight.Bold, color = CharcoalDark, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tambahkan furnitur dari katalog atau Virtual Try-On.", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenShopping,
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Jelajahi Katalog", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                groupedItems.forEach { (sceneLabel, sceneItems) ->
                    item {
                        Surface(
                            color = SurfaceVariantWarm,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (sceneLabel.startsWith("demo_scene_")) "Kamar Kost Aesthetic Japandi" else sceneLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalDark
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${sceneItems.size} produk", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    items(sceneItems) { item ->
                        val prod = item.product
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { onToggleItem(prod.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = CharcoalDark)
                                )

                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantWarm)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CharcoalDark,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${prod.marketplace.displayName} • ${prod.getFormattedPrice()}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Quantity Stepper
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { onUpdateQuantity(prod.id, item.quantity - 1) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = { onUpdateQuantity(prod.id, item.quantity + 1) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onUpdateQuantity(prod.id, 0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TerracottaRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bulk Checkout Affiliate Simulation Modal
    if (showCheckoutModal) {
        AlertDialog(
            onDismissRequest = { showCheckoutModal = false },
            title = {
                Text("Checkout Afiliasi Marketplace (Simulasi)", fontWeight = FontWeight.Bold, color = CharcoalDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RuangAI akan mengalihkan ${selectedItems.size} produk terpilih ke keranjang belanja masing-masing marketplace mitra:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    selectedItems.forEach { itm ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("• ${itm.product.title} (${itm.quantity}x)", fontSize = 11.sp, color = CharcoalDark, maxLines = 1, modifier = Modifier.weight(1f))
                            Text(itm.product.marketplace.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(itm.product.marketplace.badgeColor))
                        }
                    }
                    HorizontalDivider(color = SurfaceVariantWarm)
                    Text(
                        text = "Transparansi: Pembelian melalui tautan afiliasi ini mendukung keberlangsungan platform RuangAI tanpa biaya tambahan bagi Anda.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCheckoutModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("Buka Marketplace", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutModal = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}
