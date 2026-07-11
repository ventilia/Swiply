package com.swiply.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * Брендовые константы. Дизайн v2 — без градиентов: единый сплошной бренд-цвет
 * (роза) плюс сдержанные акценты. Виолет остаётся лишь как вторичный тон.
 */
object SwiplyPalette {
    val Rose = Color(0xFFFF4D6D) // основной бренд-цвет (like, CTA, акценты)
    val RoseDim = Color(0xFFE23E5C) // нажатое состояние / тёмная тема
    val Violet = Color(0xFF7B3FE4) // вторичный акцент (очень дозированно)
    val Teal = Color(0xFF00C2A8) // суперлайк / успех
    val Red = Color(0xFFFF4D4D) // ошибки
    val NopeRed = Color(0xFFFF5C5C) // «дальше»
    val Green = Color(0xFF3DD68C) // онлайн-статус
    val Amber = Color(0xFFFFB020)

    // Светлая тема — тёплая бумага
    val LightBackground = Color(0xFFFBF9F8)
    val LightSurface = Color(0xFFFFFFFF)
    val LightElevated = Color(0xFFF3EFF4)
    val LightTextPrimary = Color(0xFF17161A)
    val LightTextSecondary = Color(0xFF6B6873)
    val LightOutline = Color(0xFFEAE6EC)

    // Тёмная тема — глубокий near-black с лёгким фиолетовым подтоном
    val DarkBackground = Color(0xFF0F0E13)
    val DarkSurface = Color(0xFF1A1922)
    val DarkElevated = Color(0xFF242231)
    val DarkTextPrimary = Color(0xFFF4F2F8)
    val DarkTextSecondary = Color(0xFF9E99AB)
    val DarkOutline = Color(0xFF2E2C3A)
}

/**
 * Расширение Material-палитры: бренд-цвет и цвета свайп-действий, которых нет
 * в стандартной ColorScheme.
 */
@Immutable
data class SwiplyColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    /** Единый брендовый цвет (v2). Раньше был градиент — теперь сплошной. */
    val brand: Color,
    val brandPressed: Color,
    /** Мягкая подложка бренд-цвета (контейнеры, выделения) */
    val brandContainer: Color,
    val like: Color,
    val nope: Color,
    val superlike: Color,
    val success: Color,
    val danger: Color,
    val online: Color,
) {
    // ==== Слой совместимости со старым «градиентным» API ====
    // Градиенты удалены (дизайн v2): всё, что раньше заливалось градиентом,
    // теперь рисуется сплошным бренд-цветом.
    val gradientStart: Color get() = brand
    val gradientEnd: Color get() = brand
    val brandGradient: Brush get() = SolidColor(brand)
    val brandGradientHorizontal: Brush get() = SolidColor(brand)
}

val LightSwiplyColors = SwiplyColors(
    background = SwiplyPalette.LightBackground,
    surface = SwiplyPalette.LightSurface,
    surfaceElevated = SwiplyPalette.LightElevated,
    textPrimary = SwiplyPalette.LightTextPrimary,
    textSecondary = SwiplyPalette.LightTextSecondary,
    outline = SwiplyPalette.LightOutline,
    brand = SwiplyPalette.Rose,
    brandPressed = SwiplyPalette.RoseDim,
    brandContainer = Color(0xFFFFE7EC),
    like = SwiplyPalette.Rose,
    nope = SwiplyPalette.NopeRed,
    superlike = SwiplyPalette.Teal,
    success = SwiplyPalette.Teal,
    danger = SwiplyPalette.Red,
    online = SwiplyPalette.Green,
)

val DarkSwiplyColors = SwiplyColors(
    background = SwiplyPalette.DarkBackground,
    surface = SwiplyPalette.DarkSurface,
    surfaceElevated = SwiplyPalette.DarkElevated,
    textPrimary = SwiplyPalette.DarkTextPrimary,
    textSecondary = SwiplyPalette.DarkTextSecondary,
    outline = SwiplyPalette.DarkOutline,
    brand = SwiplyPalette.Rose,
    brandPressed = SwiplyPalette.RoseDim,
    brandContainer = Color(0xFF3A1F29),
    like = SwiplyPalette.Rose,
    nope = SwiplyPalette.NopeRed,
    superlike = SwiplyPalette.Teal,
    success = SwiplyPalette.Teal,
    danger = SwiplyPalette.Red,
    online = SwiplyPalette.Green,
)

val LocalSwiplyColors = staticCompositionLocalOf { LightSwiplyColors }
