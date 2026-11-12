package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarmCamelLight,
    onPrimary = CharcoalDark,
    primaryContainer = CharcoalMedium,
    onPrimaryContainer = WarmLinen,
    secondary = MutedTaupe,
    onSecondary = PureWhite,
    background = CharcoalDark,
    onBackground = WarmLinen,
    surface = CharcoalMedium,
    onSurface = WarmLinen,
    surfaceVariant = CharcoalDark,
    onSurfaceVariant = MutedTaupe,
    outline = MutedTaupe,
    tertiary = ForestGreen
)

private val LightColorScheme = lightColorScheme(
    primary = CharcoalDark,
    onPrimary = PureWhite,
    primaryContainer = SurfaceVariantWarm,
    onPrimaryContainer = CharcoalDark,
    secondary = MutedTaupe,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceVariantWarm,
    onSecondaryContainer = CharcoalDark,
    background = WarmLinen,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantWarm,
    onSurfaceVariant = TextSecondary,
    outline = OutlineWarm,
    tertiary = WarmCamelAccent,
    onTertiary = PureWhite,
    error = TerracottaRed,
    onError = PureWhite
)

@Composable
fun RuangAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
