package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.ruangai.domain.model.UserSession
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    userSession: UserSession?,
    savedProjectsCount: Int,
    creditCount: Int,
    onOpenWallet: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Profil Pengguna",
                subtitle = "Pengaturan akun, privasi & riwayat"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CharcoalDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userSession?.displayName?.firstOrNull() ?: 'U').toString(),
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userSession?.displayName ?: "Pengguna RuangAI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark
                            )
                        )
                        Text(
                            text = userSession?.email ?: "dimas.pratama@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Stats Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Proyek Ruangan", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$savedProjectsCount Proyek", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenWallet)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Kredit Tersedia", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$creditCount Kredit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarmCamelAccent)
                    }
                }
            }

            // Settings Menu Section
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Wallet,
                        title = "Dompet Kredit AI",
                        subtitle = "Beli kredit dan lihat riwayat render",
                        onClick = onOpenWallet
                    )
                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        title = "Privasi & Kepatuhan UU PDP",
                        subtitle = "Data foto ruangan terenkripsi & privat",
                        onClick = { showPrivacyDialog = true }
                    )
                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi Desain",
                        subtitle = "Belum Tersedia (Menunggu Backend)",
                        onClick = { showNotificationDialog = true }
                    )
                }
            }

            // Account Actions
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Keluar Akun",
                        subtitle = "Akhiri sesi pada perangkat ini",
                        iconTint = CharcoalDark,
                        onClick = onLogout
                    )
                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))
                    ProfileMenuItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Hapus Akun Saya",
                        subtitle = "Belum Tersedia (Menunggu Backend)",
                        iconTint = TextMuted,
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            // Platform Info
            Text(
                text = "RuangAI v1.0.0 (MVP) • Build Preview",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privasi & Kepatuhan UU PDP", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "RuangAI berkomitmen melindungi privasi data visual dan identitas Anda sesuai UU Pelindungan Data Pribadi No. 27/2022:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharcoalDark
                    )
                    Text("• Foto ruangan disimpan secara privat di penyimpanan internal perangkat Anda.", fontSize = 12.sp, color = TextSecondary)
                    Text("• RuangAI tidak menjual atau membagikan foto ruangan Anda kepada pihak ketiga.", fontSize = 12.sp, color = TextSecondary)
                    Text("• Tautan belanja marketplace menggunakan Sub-ID tanpa identitas pribadi (non-PII).", fontSize = 12.sp, color = TextSecondary)
                    Text("• Anda berhak menghapus seluruh proyek ruangan dan riwayat konsultasi kapan saja.", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("Tutup", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Notification Unavailable Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Pengaturan Notifikasi", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Fitur notifikasi push untuk promo furnitur dan pembaruan desain saat ini belum aktif. Fitur ini akan tersedia setelah integrasi server push notification pada Milestone 8B.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("Mengerti", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Delete Account Confirmation Modal (Marked unavailable until backend support)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Penghapusan Akun Belum Tersedia", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Fitur penghapusan akun permanen saat ini belum tersedia dalam mode pratinjau lokal. Sesuai UU Pelindungan Data Pribadi (UU PDP No. 27/2022), penghapusan data cloud dan pembatalan akun akan diaktifkan setelah integrasi backend selesai pada Milestone 8B.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("Mengerti", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = CharcoalDark,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CharcoalDark)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}
