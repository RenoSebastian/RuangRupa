package com.example.ruangai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.*
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.FurnitureCutoutImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsultationScreen(
    chatMessages: List<ChatMessage>,
    isAiThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToTryOn: () -> Unit,
    onNavigateToShopping: () -> Unit,
    activeScene: RoomScene? = null,
    allScenes: List<RoomScene> = emptyList(),
    activeConversation: ConsultationConversation? = null,
    pendingAttachments: List<ChatAttachment> = emptyList(),
    chatError: String? = null,
    comparedProductIds: List<String> = emptyList(),
    onSelectScene: (String) -> Unit = {},
    onAttachPhoto: (String, String) -> Unit = { _, _ -> },
    onRemovePendingAttachment: (String) -> Unit = {},
    onClearConversation: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onOpenStyleExplorer: () -> Unit = {},
    onUpdateDimensionsAndBudget: (DimensionsCm?, Long) -> Unit = { _, _ -> },
    onViewProductDetail: (String) -> Unit = {},
    onCompareProduct: (String) -> Unit = {},
    onAddProductToShoppingList: (String) -> Unit = {},
    onTryProductInRoom: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var lastUserPrompt by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showContextEditDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAttachPhoto(uri.toString(), "Foto Referensi ${activeScene?.title ?: "Ruangan"}")
        }
    }

    val suggestedPrompts = listOf(
        "Cari sofa di bawah Rp1,5 juta",
        "Buat kamar ini lebih mewah dengan budget Rp2 juta",
        "Bagaimana cara membuat ruang tamu terlihat lebih luas?",
        "Saya ingin gaya Japandi dengan warna kayu natural"
    )

    LaunchedEffect(chatMessages.size, isAiThinking, activeScene?.id) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val activeStyle = activeConversation?.selectedStyle ?: activeScene?.preferredStyle ?: DesignStyle.JAPANDI
    val effectiveBudget = activeConversation?.customBudgetIdr ?: activeScene?.targetBudget ?: 5_000_000L

    Scaffold(
        topBar = {
            AppHeader(
                title = "Konsultan Desain AI",
                subtitle = activeScene?.title ?: "Tanya jawab gaya, furnitur & budget",
                onBackClick = onBack,
                actionContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onOpenStyleExplorer,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("chat_open_style_explorer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Style Explorer",
                                tint = CharcoalDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("chat_clear_conversation_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Bersihkan Percakapan",
                                tint = TerracottaRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // 1. Simulated AI Disclaimer Banner
            Surface(
                color = SurfaceVariantWarm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WarmCamelAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mode Simulasi Konsultan AI • Rekomendasi terhubung ke katalog & proyek ruangan Anda",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // 2. Room Project Switcher & Active Context Bar
            if (activeScene != null) {
                Surface(
                    color = PureWhite,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (allScenes.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                items(allScenes, key = { it.id }) { sceneItem ->
                                    val isSelected = sceneItem.id == activeScene.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CharcoalDark else WarmLinen,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) CharcoalDark else OutlineWarm
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onSelectScene(sceneItem.id) }
                                            .testTag("chat_scene_chip_${sceneItem.id}")
                                    ) {
                                        Text(
                                            text = sceneItem.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PureWhite else CharcoalDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Style Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WarmCamelAccent.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onOpenStyleExplorer() }
                                ) {
                                    Text(
                                        text = "Gaya: ${activeStyle.displayName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Budget Badge
                                val fmtBudget = "Rp " + "%,d".format(effectiveBudget).replace(',', '.')
                                Text(
                                    text = "Budget: $fmtBudget",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )

                                // Dimensions Badge
                                val dimLabel = activeScene.userDimensionsCm?.let {
                                    "${it.width}x${it.depth}cm"
                                } ?: "Ukuran: Auto"
                                Text(
                                    text = "• $dimLabel",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }

                            TextButton(
                                onClick = { showContextEditDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Atur Ukuran/Budget",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmCamelAccent
                                )
                            }
                        }
                    }
                }
            }

            // 3. Error & Retry Banner
            if (chatError != null) {
                Surface(
                    color = TerracottaRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TerracottaRed.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chatError,
                            fontSize = 12.sp,
                            color = TerracottaRed,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            if (lastUserPrompt.isNotBlank()) {
                                TextButton(onClick = { onSendMessage(lastUserPrompt) }) {
                                    Text("Coba Lagi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                                }
                            }
                            IconButton(onClick = onDismissError, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 4. Message Stream or Empty State
            if (chatMessages.isEmpty() && !isAiThinking) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = null,
                            tint = WarmCamelAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mulai Konsultasi Interior",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CharcoalDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tanyakan saran penataan ruangan, kombinasi palet warna, atau rekomendasi furnitur sesuai anggaran proyek Anda.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("chat_message_list")
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        val isLatestAssistantMessage = !msg.isFromUser && msg.id == chatMessages.lastOrNull()?.id
                        ChatBubbleItem(
                            message = msg,
                            enableStreamingEffect = isLatestAssistantMessage,
                            comparedProductIds = comparedProductIds,
                            onActionClick = { action ->
                                when {
                                    action.contains("Style", ignoreCase = true) || action.contains("Gaya", ignoreCase = true) -> {
                                        onOpenStyleExplorer()
                                    }
                                    action.contains("Furnitur", ignoreCase = true) || action.contains("Ruangan", ignoreCase = true) -> {
                                        if (action.startsWith("Cari") || action.startsWith("Buat") || action.startsWith("Bagaimana")) {
                                            lastUserPrompt = action
                                            onSendMessage(action)
                                        } else {
                                            onNavigateToTryOn()
                                        }
                                    }
                                    action.contains("Belanja", ignoreCase = true) -> {
                                        onNavigateToShopping()
                                    }
                                    else -> {
                                        lastUserPrompt = action
                                        onSendMessage(action)
                                    }
                                }
                            },
                            onViewProductDetail = onViewProductDetail,
                            onCompareProduct = onCompareProduct,
                            onAddProductToShoppingList = onAddProductToShoppingList,
                            onTryProductInRoom = onTryProductInRoom
                        )
                    }

                    if (isAiThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp)
                                    .testTag("chat_typing_indicator")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = WarmCamelAccent.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = CharcoalDark,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    color = WarmCamelAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Konsultan RuangAI sedang menyusun rekomendasi...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // 5. Staged Photo Attachments Strip (Before Sending)
            if (pendingAttachments.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantWarm)
                ) {
                    items(pendingAttachments, key = { it.id }) { att ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PureWhite,
                            border = BorderStroke(1.dp, WarmCamelAccent),
                            modifier = Modifier.testTag("pending_attachment_${att.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = WarmCamelAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = att.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark
                                    )
                                    Text(
                                        text = "Lampiran Simulasi Lokal",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onRemovePendingAttachment(att.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Lampiran",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Suggested Prompt Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestedPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, OutlineWarm),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                lastUserPrompt = prompt
                                onSendMessage(prompt)
                            }
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // 7. Input Bar with Photo Picker Attachment Button
            Surface(
                color = PureWhite,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = WarmLinen,
                            contentColor = CharcoalDark
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Lampirkan Foto Ruangan",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Tanya gaya, ukuran, atau budget...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CharcoalDark,
                            unfocusedBorderColor = OutlineWarm,
                            focusedContainerColor = WarmLinen,
                            unfocusedContainerColor = WarmLinen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() || pendingAttachments.isNotEmpty()) {
                                lastUserPrompt = inputText
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = CharcoalDark,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Clear Conversation Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Bersihkan Riwayat Konsultasi?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Seluruh pesan dan lampiran foto pada konsultasi proyek '${activeScene?.title ?: "ini"}' akan dihapus dan direset ke awal.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearConversation()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed)
                ) {
                    Text("Ya, Bersihkan", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = CharcoalDark)
                }
            }
        )
    }

    // Edit Room Dimensions & Target Budget Dialog
    if (showContextEditDialog && activeScene != null) {
        var widthText by remember(activeScene) {
            mutableStateOf((activeScene.userDimensionsCm?.width ?: 300).toString())
        }
        var depthText by remember(activeScene) {
            mutableStateOf((activeScene.userDimensionsCm?.depth ?: 400).toString())
        }
        var heightText by remember(activeScene) {
            mutableStateOf((activeScene.userDimensionsCm?.height ?: 280).toString())
        }
        var budgetText by remember(activeScene, effectiveBudget) {
            mutableStateOf(effectiveBudget.toString())
        }

        AlertDialog(
            onDismissRequest = { showContextEditDialog = false },
            title = { Text("Konteks Ukuran & Anggaran", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Perbarui ukuran fisik dan target budget proyek '${activeScene.title}' agar rekomendasi furnitur semakin akurat.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthText,
                            onValueChange = { widthText = it.filter { c -> c.isDigit() } },
                            label = { Text("Lebar (cm)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = depthText,
                            onValueChange = { depthText = it.filter { c -> c.isDigit() } },
                            label = { Text("Panjang (cm)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                            label = { Text("Tinggi (cm)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Anggaran (IDR)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = widthText.toIntOrNull()
                        val d = depthText.toIntOrNull()
                        val h = heightText.toIntOrNull()
                        val dims = if (w != null && d != null && h != null && w > 0 && d > 0 && h > 0) {
                            DimensionsCm(w, d, h)
                        } else null
                        val newBudget = budgetText.toLongOrNull()?.coerceAtLeast(100_000L) ?: effectiveBudget
                        onUpdateDimensionsAndBudget(dims, newBudget)
                        showContextEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark)
                ) {
                    Text("Simpan Konteks", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContextEditDialog = false }) {
                    Text("Batal", color = CharcoalDark)
                }
            }
        )
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    enableStreamingEffect: Boolean = false,
    comparedProductIds: List<String> = emptyList(),
    onActionClick: (String) -> Unit,
    onViewProductDetail: (String) -> Unit = {},
    onCompareProduct: (String) -> Unit = {},
    onAddProductToShoppingList: (String) -> Unit = {},
    onTryProductInRoom: (String) -> Unit = {}
) {
    val isUser = message.isFromUser
    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    // Streaming-style progressive reveal for newly generated assistant messages
    var displayedLength by remember(message.id) {
        val isRecent = (System.currentTimeMillis() - message.timestamp) < 2500L
        mutableIntStateOf(if (enableStreamingEffect && !isUser && isRecent) 24 else message.text.length)
    }

    LaunchedEffect(message.id, enableStreamingEffect) {
        while (displayedLength < message.text.length) {
            delay(12)
            displayedLength = (displayedLength + 18).coerceAtMost(message.text.length)
        }
    }

    val visibleText = if (displayedLength >= message.text.length) {
        message.text
    } else {
        message.text.take(displayedLength) + " ▍"
    }

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar + Sender & Timestamp Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = WarmCamelAccent.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Konsultan AI",
                            tint = CharcoalDark,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Konsultan RuangAI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceVariantWarm
                ) {
                    Text(
                        text = "Simulasi Demo",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            } else {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Anda",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = CharcoalDark,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Pengguna",
                            tint = PureWhite,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // Multimodal Attachments (if any)
        if (message.attachments.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                message.attachments.forEach { att ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, OutlineWarm),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (att.fileExists) Icons.Default.Image else Icons.Default.BrokenImage,
                                    contentDescription = null,
                                    tint = if (att.fileExists) WarmCamelAccent else TerracottaRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = att.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalDark
                                    )
                                    Text(
                                        text = if (att.fileExists) {
                                            att.simulationNote
                                        } else {
                                            "File lampiran lokal tidak ditemukan (telah dihapus atau dipindahkan)"
                                        },
                                        fontSize = 10.sp,
                                        color = if (att.fileExists) TextMuted else TerracottaRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary Message Text Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isUser) CharcoalDark else PureWhite,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = visibleText,
                color = if (isUser) PureWhite else CharcoalDark,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(14.dp)
            )
        }

        // Embedded Product Recommendation Cards (Part D)
        if (!isUser && message.recommendedProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Rekomendasi Furnitur Katalog (${message.recommendedProducts.size} Produk):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                message.recommendedProducts.forEach { rec ->
                    val isCompared = comparedProductIds.contains(rec.productId)
                    RecommendedProductChatCard(
                        item = rec,
                        isCompared = isCompared,
                        onViewDetail = { onViewProductDetail(rec.productId) },
                        onCompare = { onCompareProduct(rec.productId) },
                        onAddToCart = { onAddProductToShoppingList(rec.productId) },
                        onTryInRoom = { onTryProductInRoom(rec.productId) }
                    )
                }
            }
        }

        // Suggested Action Chips
        if (!isUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(message.suggestedActions) { act ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarmCamelAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, WarmCamelAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onActionClick(act) }
                    ) {
                        Text(
                            text = act,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedProductChatCard(
    item: ProductRecommendationItem,
    isCompared: Boolean,
    onViewDetail: () -> Unit,
    onCompare: () -> Unit,
    onAddToCart: () -> Unit,
    onTryInRoom: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, OutlineWarm),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_rec_card_${item.productId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarmLinen,
                    modifier = Modifier.size(64.dp)
                ) {
                    FurnitureCutoutImage(
                        productId = item.productId,
                        productTitle = item.productTitle,
                        categoryName = item.categoryName,
                        colorHex = 0xFFD4B393,
                        dimensions = item.dimensions,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarmCamelAccent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = item.styleCompatibilityLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.fitsBudget) SageGreenSuccess.copy(alpha = 0.16f) else TerracottaRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (item.fitsBudget) "Sesuai Budget" else "Over Budget",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.fitsBudget) SageGreenSuccess else TerracottaRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.productTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${item.getFormattedPrice()} • ${item.dimensions.toDisplayString()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmCamelAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.reason,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4 Functional Action Buttons (Detail, Compare, Add to Cart, Try in Room)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .testTag("rec_detail_${item.productId}")
                ) {
                    Text("Detail", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CharcoalDark)
                }

                OutlinedButton(
                    onClick = onCompare,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isCompared) WarmCamelAccent else OutlineWarm),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .testTag("rec_compare_${item.productId}")
                ) {
                    Text(
                        text = if (isCompared) "Dibandingkan" else "Bandingkan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompared) WarmCamelAccent else CharcoalDark
                    )
                }

                OutlinedButton(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .testTag("rec_cart_${item.productId}")
                ) {
                    Text("+ Belanja", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CharcoalDark)
                }

                Button(
                    onClick = onTryInRoom,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(32.dp)
                        .testTag("rec_tryon_${item.productId}")
                ) {
                    Text("Coba di Kamar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
        }
    }
}
