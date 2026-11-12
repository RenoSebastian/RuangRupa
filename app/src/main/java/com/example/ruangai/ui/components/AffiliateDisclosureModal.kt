package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.AffiliateRedirectInfo
import com.example.ruangai.domain.model.Product
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffiliateDisclosureModal(
    product: Product,
    affiliateInfo: AffiliateRedirectInfo,
    onConfirmRedirect: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WarmLinen,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(product.marketplace.badgeColor).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(product.marketplace.badgeColor),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pengalihan Mitra Belanja",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                    )
                    Text(
                        text = "Mitra Resmi: ${product.marketplace.displayName} Indonesia",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transparent Affiliate Disclosure Banner (Regulated Compliance)
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarmCamelAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Transparansi Afiliasi: RuangAI dapat memperoleh komisi dari pembelian yang dilakukan melalui tautan ini tanpa ada penambahan biaya apa pun bagi Anda. Harga yang Anda bayar tetap sama dengan harga resmi di ${product.marketplace.displayName}.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attribution Tracking Metadata Card (CTO Rules: Real Schema & Sub-ID Transparency)
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Parameter Atribusi Sub-ID (Simulasi)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CharcoalDark
                    )
                    HorizontalDivider(color = SurfaceVariantWarm)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Produk:", fontSize = 11.sp, color = TextMuted)
                        Text(product.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalDark, maxLines = 1)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sub-ID 1 (Proyek):", fontSize = 11.sp, color = TextMuted)
                        Text(affiliateInfo.trackingMetadata.subId1, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CharcoalDark)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sub-ID 2 (User Hash):", fontSize = 11.sp, color = TextMuted)
                        Text(affiliateInfo.trackingMetadata.subId2, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CharcoalDark)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sub-ID 3 (Slot Penempatan):", fontSize = 11.sp, color = TextMuted)
                        Text(affiliateInfo.trackingMetadata.subId3, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CharcoalDark)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Status Afiliasi:", fontSize = 11.sp, color = TextMuted)
                        Surface(
                            color = ForestGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE_SIMULATED",
                                color = ForestGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = onConfirmRedirect,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(product.marketplace.badgeColor)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("affiliate_confirm_redirect_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Beli di ${product.marketplace.displayName} (${product.getFormattedPrice()})",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("Batal & Kembali ke Aplikasi", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
