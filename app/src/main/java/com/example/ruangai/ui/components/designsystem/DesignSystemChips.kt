package com.example.ruangai.ui.components.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Transparent Affiliate Disclosure Badge complying with UU Perlindungan Konsumen & Advertising Policies
 */
@Composable
fun AffiliateBadge(
    modifier: Modifier = Modifier,
    text: String = "Afiliasi / Komisi diperoleh",
    testTag: String = "affiliate_disclosure_badge"
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = WarmMuted.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.35f)),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            )
        }
    }
}

/**
 * Category & Style Filter Chip with standard 48dp touch target support
 */
@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) CharcoalDark else SurfaceWarm,
        border = if (isSelected) null else BorderStroke(1.dp, OutlineWarm.copy(alpha = 0.7f)),
        modifier = modifier
            .heightIn(min = 36.dp)
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) PureWhite else CharcoalDark,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }
    }
}

/**
 * Price Tag with Indonesian Rupiah formatting and optional discount pill
 */
@Composable
fun PriceTagBadge(
    priceIdr: Long,
    modifier: Modifier = Modifier,
    originalPriceIdr: Long? = null,
    testTag: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = "Rp ${String.format("%,d", priceIdr).replace(',', '.')}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalDark,
                fontSize = 15.sp
            )
        )

        if (originalPriceIdr != null && originalPriceIdr > priceIdr) {
            val discountPercent = ((originalPriceIdr - priceIdr) * 100 / originalPriceIdr).toInt()
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SageGreenSuccess.copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = SageGreenSuccess,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "-$discountPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageGreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Status indicator badge (Success, Warning, Info, Neutral)
 */
enum class BadgeStatusType {
    SUCCESS,
    WARNING,
    INFO,
    NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    statusType: BadgeStatusType = BadgeStatusType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (statusType) {
        BadgeStatusType.SUCCESS -> Pair(SageGreenSuccess.copy(alpha = 0.15f), SageGreenSuccess)
        BadgeStatusType.WARNING -> Pair(TerracottaError.copy(alpha = 0.15f), TerracottaError)
        BadgeStatusType.INFO -> Pair(WarmCamelAccent.copy(alpha = 0.15f), WarmCamelAccent)
        BadgeStatusType.NEUTRAL -> Pair(SurfaceWarm, TextMuted)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
