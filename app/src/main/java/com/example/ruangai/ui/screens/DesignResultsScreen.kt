package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.DesignHotspotPin
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.BeforeAfterSlider
import com.example.ui.theme.*

@Composable
fun DesignResultsScreen(
    scene: RoomScene,
    onBack: () -> Unit,
    onOpenTryOn: (String) -> Unit,
    onOpenShopping: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val makeover = scene.fullMakeovers.firstOrNull()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Hasil Desain Ruangan",
                subtitle = "${scene.title} • Mode Makeover Penuh",
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
                .padding(20.dp)
        ) {
            // Interactive Before/After Split Slider
            if (makeover != null) {
                BeforeAfterSlider(
                    beforeImageUrl = scene.originalImage.localUri,
                    afterImageUrl = makeover.renderImageUrl
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style Badge & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarmCamelAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Tema: ${makeover?.style?.displayName ?: "Japandi"}",
                        color = CharcoalDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Estimasi Total: Rp " + "%,d".format(makeover?.estimatedBudgetTotalIdr ?: 2500000).replace(',', '.'),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Design Summary Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhite,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Konsep Desain:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CharcoalDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = makeover?.designSummary ?: "Transformasi minimalis dengan pengoptimalan pencahayaan dan furnitur fungsional.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Hotspot Pins (Furnitur di Desain)
            Text(
                text = "Furnitur Terpasang di Desain:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            makeover?.hotspotPins?.forEach { pin ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WarmCamelAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pin.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text("Rp " + "%,d".format(pin.priceIdr).replace(',', '.'), fontSize = 12.sp, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = onOpenShopping,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Lihat", fontSize = 11.sp, color = CharcoalDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action: Mode B Virtual Try-On Launcher
            Button(
                onClick = { onOpenTryOn(scene.id) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("results_btn_tryon")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Ganti Furnitur (Virtual Try-On)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PureWhite)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRegenerate,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Ulang Variasi Lain", color = CharcoalDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}
