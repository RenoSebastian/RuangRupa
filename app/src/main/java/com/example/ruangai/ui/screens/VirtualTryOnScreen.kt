package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.data.local.graphics.CompositeExportResult
import com.example.ruangai.domain.model.PlacementTransform
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.ProductCategory
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.CreditConfirmationDialog
import com.example.ruangai.ui.components.VirtualTryOnCanvas
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualTryOnScreen(
    scene: RoomScene,
    selectedPlacementId: String?,
    allProducts: List<Product>,
    walletBalance: Int,
    isGeneratingPreview: Boolean,
    showCreditDialog: Boolean,
    successNotice: String?,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    isGridEnabled: Boolean = false,
    lastExportResult: CompositeExportResult? = null,
    onSelectPlacement: (String?) -> Unit,
    onTransformChange: (String, PlacementTransform) -> Unit,
    onRemovePlacement: (String) -> Unit,
    onReplaceProduct: (String, Product) -> Unit,
    onAddProductToScene: (Product) -> Unit,
    onBringToFront: (String) -> Unit = {},
    onSendToBack: (String) -> Unit = {},
    onResetTransform: (String) -> Unit = {},
    onToggleGrid: () -> Unit = {},
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onExportComposite: () -> Unit = {},
    onRequestRealisticPreview: () -> Unit,
    onConfirmCreditRender: () -> Unit,
    onDismissCreditDialog: () -> Unit,
    onBack: () -> Unit,
    onOpenShoppingList: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProductPickerSheet by remember { mutableStateOf(false) }
    var replacePlacementTargetId by remember { mutableStateOf<String?>(null) }
    var selectedPickerCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastExportResult) {
        if (lastExportResult != null) {
            showExportSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            Column {
                AppHeader(
                    title = "Virtual Try-On 2D",
                    subtitle = "${scene.title} • Multi-Produk",
                    onBackClick = onBack,
                    creditCount = walletBalance,
                    onCreditClick = { /* open credit wallet */ },
                    cartItemCount = scene.placements.size,
                    onCartClick = onOpenShoppingList
                )

                // Try-On Editor Toolbar (Undo, Redo, Grid, Export)
                Surface(
                    color = PureWhite,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Undo / Redo Group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onUndo,
                                enabled = canUndo,
                                modifier = Modifier.size(36.dp).testTag("tryon_btn_undo")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (canUndo) CharcoalDark else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onRedo,
                                enabled = canRedo,
                                modifier = Modifier.size(36.dp).testTag("tryon_btn_redo")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (canRedo) CharcoalDark else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Alignment Grid Toggle
                            IconButton(
                                onClick = onToggleGrid,
                                modifier = Modifier.size(36.dp).testTag("tryon_btn_grid")
                            ) {
                                Icon(
                                    imageVector = if (isGridEnabled) Icons.Default.GridOn else Icons.Default.GridOff,
                                    contentDescription = "Toggle Grid",
                                    tint = if (isGridEnabled) WarmCamelAccent else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Export Flattened Composite Action
                        OutlinedButton(
                            onClick = onExportComposite,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("tryon_btn_export_composite")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ekspor Gambar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }
            }
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
                    // Add Another Furniture Button
                    OutlinedButton(
                        onClick = {
                            replacePlacementTargetId = null
                            showProductPickerSheet = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("tryon_btn_add_furniture")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Furnitur", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Realistic AI Inpainting Preview (1 Credit)
                    Button(
                        onClick = onRequestRealisticPreview,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("tryon_btn_generate_realistic")
                    ) {
                        if (isGeneratingPreview) {
                            CircularProgressIndicator(color = WarmCamelAccent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses Inpainting...", color = PureWhite, fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pratinjau Realistis (1 Kredit)", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
            // Success Notice
            if (successNotice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ForestGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "✓ $successNotice",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Interactive Multi-Product Canvas
            VirtualTryOnCanvas(
                scene = scene,
                selectedPlacementId = selectedPlacementId,
                isGridEnabled = isGridEnabled,
                onSelectPlacement = onSelectPlacement,
                onTransformChange = onTransformChange,
                onRemovePlacement = onRemovePlacement,
                onReplaceProduct = { pid ->
                    replacePlacementTargetId = pid
                    showProductPickerSheet = true
                },
                onBringToFront = onBringToFront,
                onSendToBack = onSendToBack,
                onResetTransform = onResetTransform
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zero-Cost Virtual Try-On vs AI Inpainting Gating Explanation Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Virtual Try-On 2D (Gratis & Offline)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mode ini 100% gratis tanpa potongan kredit. Anda bebas menggeser, memutar, mengatur layer, dan mengekspor komposisi foto ruangan Anda. Pratinjau Realistis AI hanya digunakan bila Anda ingin pencahayaan dan bayangan otomatis digabungkan oleh AI.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Placements List Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Daftar Furnitur di Ruangan (${scene.placements.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                        Text(
                            text = "Total: Rp " + "%,d".format(scene.placements.sumOf { it.productPriceIdr }).replace(',', '.'),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (scene.placements.isEmpty()) {
                        Text("Belum ada furnitur terpasang. Tekan '+ Furnitur' untuk menambahkan.", fontSize = 12.sp, color = TextMuted)
                    } else {
                        scene.placements.sortedBy { it.transform.zIndex }.forEach { p ->
                            val isSelected = p.placementId == selectedPlacementId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SurfaceVariantWarm else PureWhite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPlacement(p.placementId) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Surface(
                                        color = CharcoalDark,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("L${p.transform.zIndex}", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p.productTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = CharcoalDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Rp " + "%,d".format(p.productPriceIdr).replace(',', '.'),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Result Dialog
    if (showExportSuccessDialog && lastExportResult != null) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = {
                Text("Ekspor Berhasil", fontWeight = FontWeight.Bold, color = CharcoalDark)
            },
            text = {
                Column {
                    Text(
                        "Komposisi furnitur berhasil digabungkan dengan foto asli ruangan secara non-destruktif.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Resolusi: ${lastExportResult.width} x ${lastExportResult.height} px",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalDark
                    )
                    Text(
                        "Ukuran File: ${lastExportResult.fileSizeBytes / 1024} KB",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        "File Path: ${lastExportResult.compositeFile.name}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("OK", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Credit Confirmation Dialog
    if (showCreditDialog) {
        CreditConfirmationDialog(
            currentBalance = walletBalance,
            onConfirm = onConfirmCreditRender,
            onDismiss = onDismissCreditDialog
        )
    }

    // Enhanced Filterable Product Picker Bottom Sheet
    if (showProductPickerSheet) {
        val filteredProducts = if (selectedPickerCategory == null) {
            allProducts
        } else {
            allProducts.filter { it.category == selectedPickerCategory }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showProductPickerSheet = false
                replacePlacementTargetId = null
                selectedPickerCategory = null
            },
            containerColor = WarmLinen
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (replacePlacementTargetId != null) "Pilih Furnitur Pengganti" else "Katalog Furnitur Terverifikasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                    )
                    Surface(
                        color = ForestGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Katalog Demo Terverifikasi",
                            color = ForestGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPickerCategory == null,
                        onClick = { selectedPickerCategory = null },
                        label = { Text("Semua (${allProducts.size})", fontSize = 11.sp) }
                    )
                    ProductCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedPickerCategory == cat,
                            onClick = { selectedPickerCategory = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Products List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { prod ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                // Thumbnail with transparent background
                                Surface(
                                    color = SurfaceVariantWarm,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    AsyncImage(
                                        model = prod.transparentCutoutUrl,
                                        contentDescription = prod.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark, maxLines = 1)
                                    Text(
                                        text = "${prod.category.displayName} • ${prod.dimensions.toDisplayString()}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = prod.getFormattedPrice(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ForestGreen
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (replacePlacementTargetId != null) {
                                            onReplaceProduct(replacePlacementTargetId!!, prod)
                                        } else {
                                            onAddProductToScene(prod)
                                        }
                                        showProductPickerSheet = false
                                        replacePlacementTargetId = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (replacePlacementTargetId != null) "Terapkan" else "+ Pasang",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
