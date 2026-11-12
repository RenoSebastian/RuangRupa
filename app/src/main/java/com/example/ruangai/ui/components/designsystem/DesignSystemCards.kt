package com.example.ruangai.ui.components.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ruangai.domain.model.RoomScene
import com.example.ui.theme.*

@Composable
fun RuangAiCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = PureWhite,
    borderColor: Color = OutlineWarm.copy(alpha = 0.6f),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = elevation,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .then(
                if (testTag != null) Modifier.testTag(testTag) else Modifier
            ),
        content = content
    )
}

@Composable
fun RoomProjectCard(
    scene: RoomScene,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    RuangAiCard(
        onClick = onClick,
        modifier = modifier,
        testTag = testTag ?: "room_project_${scene.id}"
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Photo & Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceWarm)
            ) {
                AsyncImage(
                    model = scene.originalImage.localUri,
                    contentDescription = scene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = scene.roomType.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Placements Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PureWhite.copy(alpha = 0.92f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = null,
                            tint = CharcoalDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${scene.placements.size} Furnitur",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CharcoalDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scene.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark,
                            fontSize = 15.sp
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Target Budget: Rp ${String.format("%,d", scene.targetBudget).replace(',', '.')}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }

                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka Proyek",
                        tint = CharcoalDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
