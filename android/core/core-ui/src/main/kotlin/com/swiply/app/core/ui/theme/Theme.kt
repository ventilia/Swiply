package com.swiply.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private fun swiplyLightColorScheme() = lightColorScheme(
    primary = SwiplyPalette.Rose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE7EC),
    onPrimaryContainer = SwiplyPalette.RoseDim,
    secondary = SwiplyPalette.Violet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE3FF),
    onSecondaryContainer = SwiplyPalette.Violet,
    tertiary = SwiplyPalette.Teal,
    onTertiary = Color.White,
    error = SwiplyPalette.Red,
    onError = Color.White,
    background = SwiplyPalette.LightBackground,
    onBackground = SwiplyPalette.LightTextPrimary,
    surface = SwiplyPalette.LightSurface,
    onSurface = SwiplyPalette.LightTextPrimary,
    surfaceVariant = SwiplyPalette.LightElevated,
    onSurfaceVariant = SwiplyPalette.LightTextSecondary,
    outline = SwiplyPalette.LightOutline,
    outlineVariant = SwiplyPalette.LightOutline,
)

private fun swiplyDarkColorScheme() = darkColorScheme(
    primary = SwiplyPalette.Rose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A1F29),
    onPrimaryContainer = Color(0xFFFFC2CE),
    secondary = Color(0xFFB08CFF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2A2140),
    onSecondaryContainer = Color(0xFFB08CFF),
    tertiary = SwiplyPalette.Teal,
    onTertiary = Color.Black,
    error = SwiplyPalette.Red,
    onError = Color.White,
    background = SwiplyPalette.DarkBackground,
    onBackground = SwiplyPalette.DarkTextPrimary,
    surface = SwiplyPalette.DarkSurface,
    onSurface = SwiplyPalette.DarkTextPrimary,
    surfaceVariant = SwiplyPalette.DarkElevated,
    onSurfaceVariant = SwiplyPalette.DarkTextSecondary,
    outline = SwiplyPalette.DarkOutline,
    outlineVariant = SwiplyPalette.DarkOutline,
)

/** Доступ к брендовым цветам: MaterialTheme.swiply.brand и т.д. */
@Suppress("UnusedReceiverParameter")
val MaterialTheme.swiply: SwiplyColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSwiplyColors.current

@Composable
fun SwiplyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val swiplyColors = if (darkTheme) DarkSwiplyColors else LightSwiplyColors
    val colorScheme = if (darkTheme) swiplyDarkColorScheme() else swiplyLightColorScheme()

    CompositionLocalProvider(LocalSwiplyColors provides swiplyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SwiplyTypography,
            shapes = SwiplyShapes,
            content = content,
        )
    }
}
