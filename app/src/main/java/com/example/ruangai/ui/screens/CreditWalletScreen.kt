package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.CreditBalance
import com.example.ruangai.domain.model.CreditTransaction
import com.example.ruangai.domain.model.TransactionType
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun CreditWalletScreen(
    balance: CreditBalance,
    transactions: List<CreditTransaction>,
    onBuyPackage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppHeader(
                title = "Dompet Kredit AI",
                subtitle = "Kelola kredit render dan riwayat pemakaian",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 40.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Balance Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CharcoalDark,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Saldo Kredit Tersedia", color = WarmLinen.copy(alpha = 0.7f), fontSize = 13.sp)
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${balance.available} Kredit",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Dalam Proses (Reserved)", color = WarmLinen.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("${balance.reserved} Kredit", color = WarmCamelAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Total Pemakaian", color = WarmLinen.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("${balance.lifetimeConsumed} Kali Render", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Explanation Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ForestGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("1 Kredit = 1 Render Realistis", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                            Text("Konsultasi chat dan pemindaian 2D gratis tanpa batas.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Top-Up Packages Section
            item {
                Text(
                    text = "Beli Paket Kredit Render:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                )
            }

            items(MockData.creditPackages) { pkg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (pkg.isPopular) PureWhite else PureWhite,
                    border = if (pkg.isPopular) androidx.compose.foundation.BorderStroke(1.5.dp, WarmCamelAccent) else androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                    shadowElevation = if (pkg.isPopular) 3.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pkg.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalDark)
                                if (pkg.badgeLabel != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (pkg.isPopular) WarmCamelAccent else ForestGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = pkg.badgeLabel,
                                            color = if (pkg.isPopular) PureWhite else ForestGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${pkg.credits} Kredit Render AI", fontSize = 12.sp, color = TextSecondary)
                            Text(pkg.getFormattedPrice(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                        }

                        Button(
                            onClick = { onBuyPackage(pkg.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (pkg.isPopular) WarmCamelAccent else CharcoalDark),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("buy_package_${pkg.id}")
                        ) {
                            Text("Beli", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }
            }

            // Transaction History
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Riwayat Transaksi Kredit:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                )
            }

            items(transactions) { tx ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.description, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = CharcoalDark)
                            Text(
                                text = when (tx.type) {
                                    TransactionType.WELCOME_BONUS -> "Bonus Pendaftaran"
                                    TransactionType.TOP_UP -> "Top Up Sukses"
                                    TransactionType.RENDER_RESERVATION -> "Reservasi"
                                    TransactionType.RENDER_CAPTURE -> "Berhasil Render"
                                    TransactionType.AUTO_REFUND -> "Pengembalian Dana"
                                },
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                            color = if (tx.amount > 0) ForestGreen else TerracottaRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
