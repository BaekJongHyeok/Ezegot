package com.jonghyeok.ezegot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EzegotColorScheme = lightColorScheme(
    primary          = Navy900,
    onPrimary        = TextOnDark,
    primaryContainer = Navy800,
    secondary        = SkyBlue400,
    onSecondary      = TextOnDark,
    secondaryContainer = SkyBlue100,
    background       = BackgroundLight,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline          = DividerColor,
    error            = ArrivalRed,
)

@Composable
fun EzegotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EzegotColorScheme,
        typography  = Typography,
        content     = content
    )
}

// Legacy alias
@Composable
fun Egegot_mkTheme(content: @Composable () -> Unit) = EzegotTheme(content)