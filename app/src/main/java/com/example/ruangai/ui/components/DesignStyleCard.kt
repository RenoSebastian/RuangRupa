package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.DesignStyle
import com.example.ui.theme.*

@Composable
fun DesignStyleCard(
    style: DesignStyle,
    onClick: (DesignStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick(style) }
            .testTag("style_card_${style.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Cover Image
            AsyncImage(
                model = style.imageUrl,
                contentDescription = style.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CharcoalDark.copy(alpha = 0.85f)
                            ),
                            startY = 60f
                        )
                    )
            )

            // Text and Palette Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = style.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = style.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WarmLinen.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color Swatches
                Row(verticalAlignment = Alignment.CenterVertically) {
                    style.primaryColors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(1.dp, PureWhite.copy(alpha = 0.6f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}
