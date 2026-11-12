package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ruangai.domain.model.RoomAnalysisResult
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.domain.model.RoomType
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.designsystem.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    scene: RoomScene?,
    analysisResult: RoomAnalysisResult?,
    onBack: () -> Unit,
    onOpenTryOn: (String) -> Unit,
    onOpenConsultation: (String) -> Unit,
    onUpdateMetadata: (String, String, RoomType, Long) -> Unit,
    onDeleteScene: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (scene == null) {
        EmptyState(
            title = "Proyek Tidak Ditemukan",
            description = "Proyek ruangan ini mungkin telah dihapus atau tidak tersedia.",
            actionText = "Kembali ke Beranda",
            onActionClick = onBack,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Edit form states
    var editTitle by remember(scene) { mutableStateOf(scene.title) }
    var editRoomType by remember(scene) { mutableStateOf(scene.roomType) }
    var editBudget by remember(scene) { mutableStateOf(scene.targetBudget.toString()) }

    Scaffold(
        topBar = {
            AppHeader(
                title = scene.title,
                subtitle = "${scene.roomType.displayName} • ${scene.placements.size} Furnitur",
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Main Room Photo Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWarm)
                    .border(1.dp, OutlineWarm, RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = scene.originalImage.localUri,
                    contentDescription = scene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Room Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = scene.roomType.displayName,
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Target Budget Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PureWhite.copy(alpha = 0.92f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "Budget: Rp ${String.format("%,d", scene.targetBudget).replace(',', '.')}",
                        color = CharcoalDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryButton(
                    text = "Buka Virtual Try-On",
                    onClick = { onOpenTryOn(scene.id) },
                    icon = Icons.Default.ViewInAr,
                    modifier = Modifier.weight(1f),
                    testTag = "detail_open_tryon"
                )

                SecondaryButton(
                    text = "Konsultasi",
                    onClick = { onOpenConsultation(scene.id) },
                    icon = Icons.Default.ChatBubbleOutline,
                    modifier = Modifier.weight(0.7f),
                    testTag = "detail_open_consultation"
                )
            }

            // Project Metadata Card
            RuangAiCard(
                testTag = "project_metadata_card"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Informasi Proyek",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark,
                                fontSize = 15.sp
                            )
                        )

                        TextButton(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = WarmCamelAccent)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(label = "Tipe Ruangan", value = scene.roomType.displayName)
                    DetailRow(label = "Furnitur Ditempatkan", value = "${scene.placements.size} item")
                    DetailRow(label = "Target Anggaran", value = "Rp ${String.format("%,d", scene.targetBudget).replace(',', '.')}")
                    DetailRow(label = "Resolusi Foto", value = "${scene.originalImage.width} x ${scene.originalImage.height} px")
                }
            }

            // Structured Vision Analysis Section (Simulasi Analisis)
            if (analysisResult != null) {
                RuangAiCard(
                    testTag = "vision_analysis_card"
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = WarmCamelAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hasil Analisis Visual",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            // Simulation Disclosure Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WarmCamelAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Simulasi Analisis",
                                    color = WarmCamelAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = analysisResult.simulationNotice,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                        )

                        HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))

                        // Dominant Colors
                        Column {
                            Text("Warna Dominan Terdeteksi:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                analysisResult.dominantColors.forEach { colorName ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceWarm,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm)
                                    ) {
                                        Text(
                                            text = colorName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CharcoalDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Surface observations
                        DetailRow(label = "Dinding & Lantai", value = analysisResult.wallAndFloorObservations)
                        DetailRow(label = "Pintu & Jendela", value = analysisResult.doorsWindows)
                        DetailRow(label = "Pencahayaan", value = analysisResult.lighting.naturalSource)
                        DetailRow(label = "Estimasi Layout", value = analysisResult.approximateLayoutObservations)

                        // Suggested Design Styles
                        Column {
                            Text("Rekomendasi Gaya Desain:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                analysisResult.suggestedDesignStyles.forEach { style ->
                                    CategoryChip(
                                        text = style.displayName,
                                        isSelected = true,
                                        onClick = { /* no-op */ }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Danger Zone: Delete Project
            RuangAiCard(
                borderColor = TerracottaError.copy(alpha = 0.4f),
                testTag = "delete_project_card"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kelola Proyek",
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Menghapus proyek ini akan membersihkan seluruh foto dan aset lokal terkait dari memori perangkat (UU PDP).",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaError),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("delete_project_btn")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Proyek Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Metadata Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ubah Informasi Proyek", fontWeight = FontWeight.Bold, color = CharcoalDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Nama Proyek") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBudget,
                        onValueChange = { editBudget = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Budget (IDR)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedBudget = editBudget.toLongOrNull() ?: scene.targetBudget
                        onUpdateMetadata(scene.id, editTitle, editRoomType, parsedBudget)
                        showEditDialog = false
                    }
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold, color = CharcoalDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Proyek Ruangan?", fontWeight = FontWeight.Bold, color = CharcoalDark) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus '${scene.title}'? Foto ruangan dan penataan furnitur akan dihapus permanen.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteScene(scene.id)
                    }
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = TerracottaError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp),
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalDark, fontWeight = FontWeight.Medium, fontSize = 12.sp),
            modifier = Modifier.weight(0.55f)
        )
    }
}
