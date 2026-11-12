package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun BeforeAfterSlider(
    beforeImageUrl: String,
    afterImageUrl: String,
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariantWarm)
            .testTag("before_after_slider")
    ) {
        val totalWidth = this.maxWidth
        val totalWidthPx = constraints.maxWidth.toFloat()

        // 1. Bottom Layer: After Image (AI Redesign)
        AsyncImage(
            model = afterImageUrl,
            contentDescription = "Hasil Desain AI",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Layer: Before Image clipped to splitFraction
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(totalWidth * splitFraction)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
        ) {
            AsyncImage(
                model = beforeImageUrl,
                contentDescription = "Foto Asli Sebelum",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth)
            )
        }

        // 3. Divider Line & Interactive Handle
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = totalWidth * splitFraction - 1.5.dp)
                .width(3.dp)
                .background(PureWhite)
        )

        // Slider Handle Knob
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = totalWidth * splitFraction - 20.dp)
                .size(40.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(CharcoalDark)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val newFraction = (splitFraction + dragAmount / totalWidthPx).coerceIn(0.05f, 0.95f)
                        splitFraction = newFraction
                    }
                }
                .testTag("slider_handle"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Geser Perbandingan",
                tint = WarmCamelAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        // 4. Badges (Before / After)
        Surface(
            color = CharcoalDark.copy(alpha = 0.7f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "Foto Asli",
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Surface(
            color = WarmCamelAccent.copy(alpha = 0.85f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Text(
                text = "Desain AI",
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Tip footer
        Surface(
            color = CharcoalDark.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Geser tombol tengah untuk membandingkan",
                color = PureWhite,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}
