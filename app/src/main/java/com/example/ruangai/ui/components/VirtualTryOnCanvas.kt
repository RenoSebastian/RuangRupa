package com.example.ruangai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.PlacementTransform
import com.example.ruangai.domain.model.ProductPlacement
import com.example.ruangai.domain.model.RoomScene
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VirtualTryOnCanvas(
    scene: RoomScene,
    selectedPlacementId: String?,
    isGridEnabled: Boolean = false,
    onSelectPlacement: (String?) -> Unit,
    onTransformChange: (String, PlacementTransform) -> Unit,
    onRemovePlacement: (String) -> Unit,
    onReplaceProduct: (String) -> Unit,
    onBringToFront: ((String) -> Unit)? = null,
    onSendToBack: ((String) -> Unit)? = null,
    onResetTransform: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedItem = scene.placements.find { it.placementId == selectedPlacementId }

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Scene Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CharcoalDark)
                .clickable { onSelectPlacement(null) }
                .testTag("virtual_try_on_canvas")
        ) {
            // 1. Immutable Original Room Backdrop (Scene Preservation)
            AsyncImage(
                model = scene.originalImage.localUri,
                contentDescription = "Foto Asli Ruangan (Immutable)",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Optional Alignment Grid Overlay
            if (isGridEnabled) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 32.dp.toPx()
                    val gridColor = Color.White.copy(alpha = 0.28f)
                    var x = step
                    while (x < size.width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }
                }
            }

            // Floor Alignment Reference Guide (Perspective anchor)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val floorY = size.height * 0.85f
                drawLine(
                    color = WarmCamelAccent.copy(alpha = 0.6f),
                    start = Offset(0f, floorY),
                    end = Offset(size.width, floorY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Protected Regions Indicator Badge
            Surface(
                color = CharcoalDark.copy(alpha = 0.75f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ForestGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scene Preservation Aktif",
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Items Count Chip
            Surface(
                color = CharcoalDark.copy(alpha = 0.75f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${scene.placements.size} Furnitur Terpasang",
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 2. Multi-Product Placements Layer (Sorted by z-index)
            scene.placements.sortedBy { it.transform.zIndex }.forEach { placement ->
                val isSelected = placement.placementId == selectedPlacementId
                val transform = placement.transform

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                            IntOffset(
                                x = transform.translationX.roundToInt(),
                                y = transform.translationY.roundToInt()
                            )
                        }
                        .scale(transform.scale)
                        .rotate(transform.rotationDegrees)
                        .size(170.dp)
                        .pointerInput(placement.placementId) {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                onSelectPlacement(placement.placementId)
                                val updated = transform.copy(
                                    translationX = transform.translationX + pan.x,
                                    translationY = transform.translationY + pan.y,
                                    scale = (transform.scale * zoom).coerceIn(0.4f, 2.5f),
                                    rotationDegrees = (transform.rotationDegrees + rotation).coerceIn(-180f, 180f)
                                )
                                onTransformChange(placement.placementId, updated)
                            }
                        }
                        .clickable { onSelectPlacement(placement.placementId) }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, WarmCamelAccent, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        )
                        .testTag("placement_item_${placement.placementId}"),
                    contentAlignment = Alignment.Center
                ) {
                    // Transparent Furniture Cutout
                    AsyncImage(
                        model = placement.referenceImage.transparentCutoutUri,
                        contentDescription = placement.productTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Corner Handles when selected
                    if (isSelected) {
                        // Top-Left corner handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset((-4).dp, (-4).dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent)
                        )
                        // Top-Right corner handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(4.dp, (-4).dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent)
                        )
                        // Bottom-Left corner handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset((-4).dp, 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent)
                        )
                        // Bottom-Right corner handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(4.dp, 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent)
                        )

                        // Bottom Category & Layer Tag
                        Surface(
                            color = CharcoalDark.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 14.dp)
                        ) {
                            Text(
                                text = "${placement.categoryName} • L${placement.transform.zIndex}",
                                color = PureWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Transform & Layer Controls Panel (Active when an item is selected)
        if (selectedItem != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Item Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedItem.productTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalDark
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Layer ${selectedItem.transform.zIndex} • Geser 1 jari untuk pindah, cubit untuk ubah ukuran",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Replace Action
                        TextButton(
                            onClick = { onReplaceProduct(selectedItem.placementId) },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Ganti", color = WarmCamelAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Remove Action
                        IconButton(
                            onClick = { onRemovePlacement(selectedItem.placementId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TerracottaRed, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceVariantWarm)

                    // Layer Ordering Actions (Bring to front / Send to back)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Urutan Layer:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )

                        OutlinedButton(
                            onClick = { onBringToFront?.invoke(selectedItem.placementId) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paling Depan", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CharcoalDark)
                        }

                        OutlinedButton(
                            onClick = { onSendToBack?.invoke(selectedItem.placementId) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.LayersClear, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paling Belakang", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scale Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ukuran",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.width(55.dp)
                        )
                        Slider(
                            value = selectedItem.transform.scale,
                            onValueChange = { newScale ->
                                onTransformChange(
                                    selectedItem.placementId,
                                    selectedItem.transform.copy(scale = newScale)
                                )
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = WarmCamelAccent,
                                activeTrackColor = WarmCamelAccent,
                                inactiveTrackColor = SurfaceVariantWarm
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(selectedItem.transform.scale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CharcoalDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    // Rotation Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rotasi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.width(55.dp)
                        )
                        Slider(
                            value = selectedItem.transform.rotationDegrees,
                            onValueChange = { newRot ->
                                onTransformChange(
                                    selectedItem.placementId,
                                    selectedItem.transform.copy(rotationDegrees = newRot)
                                )
                            },
                            valueRange = -90f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = CharcoalDark,
                                activeTrackColor = CharcoalDark,
                                inactiveTrackColor = SurfaceVariantWarm
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${selectedItem.transform.rotationDegrees.roundToInt()}°",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CharcoalDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    // Reset Button
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                onResetTransform?.invoke(selectedItem.placementId) ?: onTransformChange(
                                    selectedItem.placementId,
                                    PlacementTransform(translationX = 0f, translationY = 0f, scale = 1.0f, rotationDegrees = 0f)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Atur Ulang Posisi (Reset)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            // Helper card when no item is selected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceVariantWarm.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = WarmCamelAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ketuk furnitur di foto untuk memposisikan, mengubah ukuran, memutar, atau mengatur layer.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
