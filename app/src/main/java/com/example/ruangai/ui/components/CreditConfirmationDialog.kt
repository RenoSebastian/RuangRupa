package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CreditConfirmationDialog(
    currentBalance: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("credit_confirmation_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(WarmCamelAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WarmCamelAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Konfirmasi Render Realistis",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Gunakan 1 Kredit Render untuk integrasi pencahayaan alami, bayangan lantai, dan harmonisasi perspektif AI dengan Scene Preservation?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Summary Box
                Surface(
                    color = SurfaceVariantWarm,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text("Saldo Tersedia", fontSize = 11.sp, color = TextMuted)
                            Text("$currentBalance Kredit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                        }
                        Text("➔", fontSize = 16.sp, color = WarmCamelAccent)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sisa Saldo", fontSize = 11.sp, color = TextMuted)
                            Text("${maxOf(0, currentBalance - 1)} Kredit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Simulated Disclaimer
                Text(
                    text = "Simulasi Demo: Menggunakan integrasi mock AI pipeline tanpa biaya aktual.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_confirm_credit_render")
                    ) {
                        Text("Render (1 Kredit)", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
