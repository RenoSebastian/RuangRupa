package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.AffiliateRedirectInfo
import com.example.ui.theme.*

@Composable
fun AffiliateDisclosureBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantWarm,
        modifier = modifier
            .fillMaxWidth()
            .testTag("affiliate_disclosure_banner")
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Pengungkapan Afiliasi",
                tint = WarmCamelAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Transparansi Tautan Afiliasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CharcoalDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "RuangAI dapat memperoleh komisi dari pembelian melalui tautan afiliasi. Seluruh tautan dan harga saat ini berada dalam mode Simulasi Katalog (Demo) dan tidak mengarah ke transaksi uang riil.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun AffiliateAttributionDialog(
    redirectInfo: AffiliateRedirectInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulasi Atribusi Afiliasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CharcoalDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = ForestGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Status: ${redirectInfo.status.displayLabel}",
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = redirectInfo.disclosureLabel,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = OutlineWarm)

                AttributionMetaRow("Marketplace Tujuan", redirectInfo.marketplace.displayName)
                AttributionMetaRow("Product ID", redirectInfo.productId)
                AttributionMetaRow("Click Attribution ID", redirectInfo.clickAttributionId)
                AttributionMetaRow("Sub-ID 1 (Scene ID)", redirectInfo.trackingMetadata.subId1)
                AttributionMetaRow("Sub-ID 2 (User Hash)", redirectInfo.trackingMetadata.subId2)
                AttributionMetaRow("Sub-ID 3 (Slot ID)", redirectInfo.trackingMetadata.subId3)
                AttributionMetaRow("Kode Referensi", redirectInfo.redirectDestination.simulatedReferenceCode)

                Surface(
                    color = SurfaceVariantWarm,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Catatan: Klik ini dicatat sebagai simulasi lokal untuk pengujian atribusi desain-ke-beli, bukan pendapatan afiliasi riil.",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                modifier = Modifier.testTag("affiliate_dialog_close_btn")
            ) {
                Text("Mengerti", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PureWhite
    )
}

@Composable
private fun AttributionMetaRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(0.45f))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalDark,
            modifier = Modifier.weight(0.55f)
        )
    }
}
