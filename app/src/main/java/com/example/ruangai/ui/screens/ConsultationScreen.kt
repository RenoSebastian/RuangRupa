package com.example.ruangai.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.ChatMessage
import com.example.ruangai.domain.model.DesignStyle
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ConsultationScreen(
    chatMessages: List<ChatMessage>,
    isAiThinking: Boolean,
    activeScene: RoomScene? = null,
    allProducts: List<Product> = emptyList(),
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToTryOn: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToMakeover: () -> Unit = {},
    onOpenProductDetail: (Product) -> Unit = {},
    onTryOnProduct: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val styleQuickPills = listOf(
        DesignStyle.JAPANDI,
        DesignStyle.SCANDINAVIAN,
        DesignStyle.MINIMALIST,
        DesignStyle.TROPICAL,
        DesignStyle.INDUSTRIAL
    )

    val suggestedPrompts = listOf(
        "Rekomendasi sofa kamar kost",
        "Buat kamar tidur lebih tropis",
        "Furnitur minimalis budget Rp 2 juta",
        "Gaya Japandi untuk ruangan kompak"
    )

    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Konsultan Desain AI",
                subtitle = if (activeScene != null) "${activeScene.title} • ${activeScene.roomType.displayName}" else "Tanya jawab gaya, furnitur & budget",
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
        ) {
            // Active Room Context & Simulated AI Disclosure Bar
            Surface(
                color = SurfaceVariantWarm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeScene?.let { "Proyek Aktif: ${it.title} (Budget Rp ${"%,d".format(it.targetBudget).replace(',', '.')})" }
                                    ?: "Mode Simulasi AI: Taxonomi Interior & Katalog Terverifikasi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Style Taxonomy Quick Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(styleQuickPills) { style ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSendMessage("Terapkan konsep gaya ${style.displayName} untuk ruangan saya")
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = style.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CharcoalDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(chatMessages) { msg ->
                    val recommendedProducts = remember(msg.recommendedProductIds, allProducts) {
                        allProducts.filter { it.id in msg.recommendedProductIds }
                    }
                    ChatBubbleItem(
                        message = msg,
                        recommendedProducts = recommendedProducts,
                        onActionClick = { action ->
                            when {
                                action.contains("Makeover", ignoreCase = true) -> onNavigateToMakeover()
                                action.contains("Furnitur", ignoreCase = true) -> onNavigateToTryOn()
                                else -> onNavigateToShopping()
                            }
                        },
                        onProductClick = onOpenProductDetail,
                        onTryOnProduct = onTryOnProduct
                    )
                }

                if (isAiThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                color = WarmCamelAccent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Konsultan RuangAI sedang menganalisis proporsi & katalog...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Suggested Prompt Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestedPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
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

            // Input Bar
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
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ketik pertanyaan desain atau budget...", fontSize = 13.sp) },
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
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = CharcoalDark,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .size(48.dp)
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
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    recommendedProducts: List<Product> = emptyList(),
    onActionClick: (String) -> Unit,
    onProductClick: (Product) -> Unit = {},
    onTryOnProduct: (Product) -> Unit = {}
) {
    val isUser = message.isFromUser

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) CharcoalDark else PureWhite,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser && message.recommendedStyle != null) {
                    Surface(
                        color = WarmCamelAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Rekomendasi Gaya: ${message.recommendedStyle.displayName}",
                            color = CharcoalDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = message.text,
                    color = if (isUser) PureWhite else CharcoalDark,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // Inline Recommended Product Cards (Visual Commerce inside Chat)
        if (!isUser && recommendedProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                recommendedProducts.forEach { prod ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(prod) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantWarm)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CharcoalDark, maxLines = 1)
                                Text("${prod.getFormattedPrice()} • ${prod.dimensions.toDisplayString()}", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = { onTryOnProduct(prod) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmCamelAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Try-On", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
                            }
                        }
                    }
                }
            }
        }

        // Suggested Actions
        if (!isUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(message.suggestedActions) { act ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarmCamelAccent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmCamelAccent.copy(alpha = 0.3f)),
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
