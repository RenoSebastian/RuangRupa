package com.example.ruangai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruangai.domain.model.Product
import com.example.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onTryOnClick: (Product) -> Unit,
    isCompared: Boolean = false,
    onCompareToggle: ((Product) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        shadowElevation = 2.dp,
        border = if (isCompared) androidx.compose.foundation.BorderStroke(2.dp, WarmCamelAccent) else null,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProductClick(product) }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image with Marketplace & "Simulasi Katalog" Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(SurfaceVariantWarm)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Marketplace Badge
                Surface(
                    color = Color(product.marketplace.badgeColor),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.marketplace.displayName,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Required "Simulasi Katalog" Tag
                Surface(
                    color = CharcoalDark.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = product.demoLabel,
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Compare Toggle Button in bottom-right of image
                if (onCompareToggle != null) {
                    Surface(
                        color = if (isCompared) WarmCamelAccent else PureWhite.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clickable { onCompareToggle(product) }
                            .testTag("btn_compare_${product.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Bandingkan",
                                tint = if (isCompared) PureWhite else CharcoalDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCompared) "Dibandingkan" else "Bandingkan",
                                color = if (isCompared) PureWhite else CharcoalDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Product Details
            Column(modifier = Modifier.padding(12.dp)) {
                // Brand & Category
                Text(
                    text = "${product.brand} • ${product.category.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalDark,
                        lineHeight = 17.sp,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dimensions & Color Dot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(product.colorHex or -0x1000000))
                            .border(0.5.dp, OutlineWarm, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = product.dimensions.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price in IDR + Discount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.getFormattedPrice(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark,
                            fontSize = 14.sp
                        )
                    )
                    if (product.hasDiscount()) {
                        Surface(
                            color = ForestGreen.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "-${product.getDiscountPercentage()}%",
                                color = ForestGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                val origPrice = product.getFormattedOriginalPrice()
                if (origPrice != null) {
                    Text(
                        text = origPrice,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Style compatibility chip
                Text(
                    text = "Gaya: ${product.designStyle.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = WarmCamelAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action: Coba di Ruangan Saya (Try-On)
                Button(
                    onClick = { onTryOnClick(product) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceVariantWarm,
                        contentColor = CharcoalDark
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_try_on_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WarmCamelAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Coba di Ruangan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
