package com.example.ruangai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.data.local.storage.LocalImageStorageManager
import com.example.ruangai.domain.model.RoomAnalysisResult
import com.example.ruangai.domain.model.RoomType
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.designsystem.*
import com.example.ui.theme.*

@Composable
fun RoomScannerScreen(
    scannerStep: Int,
    progressLabel: String,
    analysisResult: RoomAnalysisResult?,
    onStartScan: (String, RoomType, String) -> Unit,
    onReset: () -> Unit,
    onFinish: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storageManager = remember { LocalImageStorageManager(context) }

    var selectedRoomType by remember { mutableStateOf(RoomType.KOST_STUDIO) }
    var projectTitle by remember { mutableStateOf("Makeover Kamar Kost") }
    var targetBudgetText by remember { mutableStateOf("5000000") }
    var roomDimensionsText by remember { mutableStateOf("3 x 4 m") }
    var selectedImageUri by remember { mutableStateOf("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=1280&q=80") }
    var referencePhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var captureErrorNotice by remember { mutableStateOf<String?>(null) }

    // 1. Android Camera Launcher via FileProvider
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri.toString()
            captureErrorNotice = null
        }
    }

    // 2. Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            captureErrorNotice = null
        }
    }

    // 3. Additional Reference Photos Picker
    val refPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            referencePhotos = (referencePhotos + uris.map { it.toString() }).take(3)
        }
    }

    val sampleGalleryImages = listOf(
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=1280&q=80" to "Kamar Kost",
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1280&q=80" to "Ruang Tamu",
        "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1280&q=80" to "Kamar Tidur",
        "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=1280&q=80" to "Ruang Kerja"
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = "Pindai Ruangan",
                subtitle = "Tangkap foto & analisis spasial ruangan",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (scannerStep) {
            0, 1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Room Photo Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceWarm)
                            .border(1.5.dp, OutlineWarm, RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto Ruangan Terpilih",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Action Controls (Retake / Remove)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.TopEnd),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CharcoalDark.copy(alpha = 0.8f),
                                modifier = Modifier.clickable {
                                    selectedImageUri = ""
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ganti Foto", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Resolution Indicator Pill
                        Surface(
                            color = CharcoalDark.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Original Asset: Tersimpan Aman",
                                color = PureWhite,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (captureErrorNotice != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TerracottaError.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaError)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TerracottaError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(captureErrorNotice!!, color = TerracottaError, fontSize = 12.sp)
                            }
                        }
                    }

                    // Media Capture Options (Camera & Gallery)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val (file, uri) = storageManager.createCameraTempUri()
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    captureErrorNotice = "Tidak dapat membuka kamera: ${e.message}"
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scanner_camera_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Kamera", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PureWhite)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, CharcoalDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scanner_pick_from_gallery")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Galeri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Sample Preloaded Rooms for Instant Testing
                    Column {
                        Text(
                            text = "Atau Gunakan Contoh Ruangan:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(sampleGalleryImages) { (uri, label) ->
                                val isChosen = selectedImageUri == uri
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isChosen) androidx.compose.foundation.BorderStroke(2.dp, WarmCamelAccent) else null,
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedImageUri = uri }
                                ) {
                                    Box {
                                        AsyncImage(model = uri, contentDescription = label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        Surface(
                                            color = CharcoalDark.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(topStart = 8.dp),
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                        ) {
                                            Text(label, color = PureWhite, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reference Angle Photos Section
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Foto Sudut Tambahan (Opsional):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                            )
                            if (referencePhotos.size < 3) {
                                TextButton(
                                    onClick = {
                                        refPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmCamelAccent)
                                }
                            }
                        }

                        if (referencePhotos.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(referencePhotos) { refUri ->
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, OutlineWarm, RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(model = refUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }

                    // Project Title Input
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Nama Proyek Ruangan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CharcoalDark,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scanner_title_input")
                    )

                    // Target Budget with Presets
                    Column {
                        OutlinedTextField(
                            value = targetBudgetText,
                            onValueChange = { targetBudgetText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Target Anggaran (IDR)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CharcoalDark,
                                focusedContainerColor = PureWhite,
                                unfocusedContainerColor = PureWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2000000" to "Rp 2 Jt", "5000000" to "Rp 5 Jt", "10000000" to "Rp 10 Jt", "15000000" to "Rp 15 Jt").forEach { (valStr, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (targetBudgetText == valStr) CharcoalDark else SurfaceWarm,
                                    modifier = Modifier.clickable { targetBudgetText = valStr }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (targetBudgetText == valStr) PureWhite else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Room Type Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tipe Ruangan:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CharcoalDark)
                        )
                        RoomType.values().forEach { rType ->
                            val isSelected = selectedRoomType == rType
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SurfaceWarm else PureWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) WarmCamelAccent else OutlineWarm
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedRoomType = rType }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedRoomType = rType },
                                        colors = RadioButtonDefaults.colors(selectedColor = WarmCamelAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(rType.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                                        Text(rType.description, fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Scan CTA
                    PrimaryButton(
                        text = "Simpan Proyek & Mulai Analisis",
                        onClick = {
                            onStartScan(selectedImageUri, selectedRoomType, projectTitle)
                        },
                        icon = Icons.Default.AutoAwesome,
                        testTag = "scanner_submit_btn"
                    )
                }
            }

            2 -> {
                // Progressive Visual Analysis State
                LoadingState(
                    message = progressLabel,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }

            3 -> {
                // Analysis Complete Report
                val res = analysisResult
                if (res != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ForestGreen.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Analisis Visual Berhasil", fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 14.sp)
                                    Text("Foto ruangan tersimpan secara permanen & batas spasial dipetakan.", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }

                        // Structured Observations
                        RuangAiCard {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ringkasan Pengamatan Ruangan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                                    StatusBadge(text = "Simulasi Analisis", statusType = BadgeStatusType.INFO)
                                }
                                HorizontalDivider(color = OutlineWarm.copy(alpha = 0.5f))

                                Text("• Dinding & Lantai: ${res.wallAndFloorObservations}", fontSize = 12.sp, color = CharcoalDark)
                                Text("• Bukaan & Jendela: ${res.doorsWindows}", fontSize = 12.sp, color = CharcoalDark)
                                Text("• Pencahayaan Alami: ${res.lighting.naturalSource}", fontSize = 12.sp, color = CharcoalDark)
                                Text("• Estimasi Layout: ${res.approximateLayoutObservations}", fontSize = 12.sp, color = CharcoalDark)
                            }
                        }

                        // Detected Items
                        Column {
                            Text("Furnitur Terdeteksi:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            res.detectedItems.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PureWhite,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceWarm),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Chair, contentDescription = null, tint = CharcoalDark, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${item.category} (${item.description})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalDark)
                                            Text(item.actionSuggestion, fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        PrimaryButton(
                            text = "Lanjutkan ke Desain & Try-On",
                            onClick = { onFinish(res.sceneId) },
                            icon = Icons.Default.ViewInAr,
                            testTag = "scanner_finish_btn"
                        )

                        GhostButton(
                            text = "Pindai Foto Lain",
                            onClick = onReset,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
