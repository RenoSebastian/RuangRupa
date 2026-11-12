package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ruangai.ui.navigation.Screen
import com.example.ui.theme.*

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val imageUrl: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Ruang Anda, Gaya Anda",
            subtitle = "Unggah foto ruangan Anda dan saksikan transformasi instan dengan berbagai gaya desain interior pilihan AI.",
            imageUrl = "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=800&q=80"
        ),
        OnboardingPageData(
            title = "Coba Sebelum Beli (Virtual Try-On)",
            subtitle = "Visualisasikan furnitur nyata langsung di foto ruangan Anda tanpa mengubah sudut, lantai, atau dinding asli (Scene Preservation).",
            imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
        ),
        OnboardingPageData(
            title = "Desain. Temukan. Belanja.",
            subtitle = "Temukan furnitur nyata dengan harga dalam Rupiah dari Shopee dan Tokopedia. Buat daftar belanja hemat sesuai budget Anda.",
            imageUrl = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80"
        )
    )

    val current = pages[currentPage]

    Surface(
        color = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.testTag("onboarding_skip")
                    ) {
                        Text(
                            text = "Lewati",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceVariantWarm)
            ) {
                AsyncImage(
                    model = current.imageUrl,
                    contentDescription = current.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Page Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentPage) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (index == currentPage) CharcoalDark else OutlineWarm)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title & Subtitle
            Text(
                text = current.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = current.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 22.sp,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Action: Next or Get Started
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_primary_btn")
            ) {
                Text(
                    text = if (currentPage < pages.size - 1) "Lanjutkan" else "Mulai Sekarang",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
