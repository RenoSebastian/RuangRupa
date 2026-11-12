package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ui.theme.*

@Composable
fun BottomTabBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home
            TabItem(
                label = BottomTab.HOME.title,
                icon = if (selectedTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                isSelected = selectedTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                tag = "tab_home"
            )

            // 2. Explore
            TabItem(
                label = BottomTab.EXPLORE.title,
                icon = if (selectedTab == BottomTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                isSelected = selectedTab == BottomTab.EXPLORE,
                onClick = { onTabSelected(BottomTab.EXPLORE) },
                tag = "tab_explore"
            )

            // 3. Elevated Design Action (Primary CTA)
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CharcoalDark)
                    .clickable { onTabSelected(BottomTab.DESIGN) }
                    .testTag("tab_design_center"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Mulai Desain",
                    tint = WarmCamelAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 4. Shopping
            TabItem(
                label = BottomTab.SHOPPING.title,
                icon = if (selectedTab == BottomTab.SHOPPING) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                isSelected = selectedTab == BottomTab.SHOPPING,
                onClick = { onTabSelected(BottomTab.SHOPPING) },
                tag = "tab_shopping"
            )

            // 5. Profile
            TabItem(
                label = BottomTab.PROFILE.title,
                icon = if (selectedTab == BottomTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                isSelected = selectedTab == BottomTab.PROFILE,
                onClick = { onTabSelected(BottomTab.PROFILE) },
                tag = "tab_profile"
            )
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val color = if (isSelected) CharcoalDark else TextMuted
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 52.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = WarmCamelAccent),
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = fontWeight,
                color = color
            )
        )
    }
}

