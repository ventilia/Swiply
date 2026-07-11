package com.swiply.app.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swiply.app.core.ui.theme.swiply

/**
 * Набор фирменных иллюстраций для пустых состояний. Всё рисуется на Canvas —
 * никаких эмодзи и внешних ассетов.
 */
enum class SwiplyIllustration {
    Discovery, Location, Likes, Matches, Chat, Blocked, Empty, Error
}

@Composable
fun SwiplyIllustrationView(
    illustration: SwiplyIllustration,
    modifier: Modifier = Modifier,
    size: Dp = 132.dp,
) {
    val brand = MaterialTheme.swiply.brand
    val soft = MaterialTheme.swiply.brandContainer
    val ink = MaterialTheme.swiply.textSecondary
    val surface = MaterialTheme.swiply.surface

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            // мягкий круг-подложка
            drawCircle(color = soft, radius = this.size.minDimension / 2f)
            val s = this.size.minDimension
            val stroke = s * 0.055f
            when (illustration) {
                SwiplyIllustration.Discovery -> drawCards(brand, ink, s, stroke)
                SwiplyIllustration.Location -> drawPin(brand, surface, s, stroke)
                SwiplyIllustration.Likes -> drawHeart(brand, s, filled = true)
                SwiplyIllustration.Matches -> drawTwoHearts(brand, ink, s)
                SwiplyIllustration.Chat -> drawBubbles(brand, ink, surface, s, stroke)
                SwiplyIllustration.Blocked -> drawShield(brand, ink, s, stroke)
                SwiplyIllustration.Empty -> drawCards(ink, ink, s, stroke)
                SwiplyIllustration.Error -> drawSpark(brand, ink, s, stroke)
            }
        }
    }
}

private fun DrawScope.drawCards(brand: Color, ink: Color, s: Float, stroke: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val w = s * 0.34f
    val h = s * 0.44f
    rotate(-12f, pivot = Offset(cx, cy)) {
        drawRoundRect(
            color = ink.copy(alpha = 0.3f),
            topLeft = Offset(cx - w / 2, cy - h / 2),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.22f),
            style = Stroke(width = stroke),
        )
    }
    rotate(9f, pivot = Offset(cx, cy)) {
        drawRoundRect(
            color = brand,
            topLeft = Offset(cx - w / 2, cy - h / 2),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.22f),
            style = Stroke(width = stroke),
        )
        // маленькое сердечко на верхней карточке
        drawHeartAt(brand, Offset(cx, cy), s * 0.14f, filled = true)
    }
}

private fun DrawScope.drawPin(brand: Color, surface: Color, s: Float, stroke: Float) {
    val cx = size.width / 2f
    val top = size.height * 0.28f
    val r = s * 0.17f
    val center = Offset(cx, top + r)
    // тело пина (круг + треугольный хвост)
    drawCircle(color = brand, radius = r, center = center)
    val tail = Path().apply {
        moveTo(cx - r * 0.72f, top + r + r * 0.5f)
        lineTo(cx, size.height * 0.72f)
        lineTo(cx + r * 0.72f, top + r + r * 0.5f)
        close()
    }
    drawPath(tail, brand)
    // «дырка» в пине
    drawCircle(color = surface, radius = r * 0.42f, center = center)
}

private fun DrawScope.drawHeart(brand: Color, s: Float, filled: Boolean) {
    drawHeartAt(brand, Offset(size.width / 2f, size.height / 2f), s * 0.24f, filled)
}

private fun DrawScope.drawHeartAt(color: Color, center: Offset, r: Float, filled: Boolean) {
    val path = Path().apply {
        val x = center.x
        val y = center.y - r * 0.5f
        moveTo(x, y + r * 0.9f)
        cubicTo(x, y + r * 0.5f, x - r, y + r * 0.2f, x - r, y - r * 0.35f)
        cubicTo(x - r, y - r * 0.9f, x - r * 0.35f, y - r * 0.95f, x, y - r * 0.4f)
        cubicTo(x + r * 0.35f, y - r * 0.95f, x + r, y - r * 0.9f, x + r, y - r * 0.35f)
        cubicTo(x + r, y + r * 0.2f, x, y + r * 0.5f, x, y + r * 0.9f)
        close()
    }
    if (filled) drawPath(path, color) else drawPath(path, color, style = Stroke(width = r * 0.22f))
}

private fun DrawScope.drawTwoHearts(brand: Color, ink: Color, s: Float) {
    drawHeartAt(ink.copy(alpha = 0.32f), Offset(size.width * 0.40f, size.height * 0.52f), s * 0.19f, filled = true)
    drawHeartAt(brand, Offset(size.width * 0.60f, size.height * 0.46f), s * 0.22f, filled = true)
}

private fun DrawScope.drawBubbles(brand: Color, ink: Color, surface: Color, s: Float, stroke: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    // задний пузырь
    drawRoundRect(
        color = ink.copy(alpha = 0.3f),
        topLeft = Offset(cx - s * 0.30f, cy - s * 0.22f),
        size = Size(s * 0.42f, s * 0.30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.13f),
        style = Stroke(width = stroke),
    )
    // передний бренд-пузырь
    drawRoundRect(
        color = brand,
        topLeft = Offset(cx - s * 0.10f, cy - s * 0.06f),
        size = Size(s * 0.42f, s * 0.30f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.13f),
    )
    // три точки в переднем пузыре
    val dotY = cy + s * 0.09f
    listOf(0.03f, 0.11f, 0.19f).forEach { off ->
        drawCircle(surface, radius = s * 0.022f, center = Offset(cx + s * off, dotY))
    }
}

private fun DrawScope.drawShield(brand: Color, ink: Color, s: Float, stroke: Float) {
    val cx = size.width / 2f
    val top = size.height * 0.28f
    val w = s * 0.34f
    val path = Path().apply {
        moveTo(cx, top)
        lineTo(cx + w, top + s * 0.09f)
        lineTo(cx + w, top + s * 0.28f)
        cubicTo(cx + w, top + s * 0.44f, cx + w * 0.5f, top + s * 0.5f, cx, top + s * 0.55f)
        cubicTo(cx - w * 0.5f, top + s * 0.5f, cx - w, top + s * 0.44f, cx - w, top + s * 0.28f)
        lineTo(cx - w, top + s * 0.09f)
        close()
    }
    drawPath(path, brand, style = Stroke(width = stroke))
    // косая черта запрета
    drawLine(
        color = brand,
        start = Offset(cx - w * 0.55f, top + s * 0.12f),
        end = Offset(cx + w * 0.55f, top + s * 0.42f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawSpark(brand: Color, ink: Color, s: Float, stroke: Float) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = s * 0.26f
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        cubicTo(c.x + r * 0.18f, c.y - r * 0.18f, c.x + r * 0.18f, c.y - r * 0.18f, c.x + r, c.y)
        cubicTo(c.x + r * 0.18f, c.y + r * 0.18f, c.x + r * 0.18f, c.y + r * 0.18f, c.x, c.y + r)
        cubicTo(c.x - r * 0.18f, c.y + r * 0.18f, c.x - r * 0.18f, c.y + r * 0.18f, c.x - r, c.y)
        cubicTo(c.x - r * 0.18f, c.y - r * 0.18f, c.x - r * 0.18f, c.y - r * 0.18f, c.x, c.y - r)
        close()
    }
    drawPath(path, brand)
}

/**
 * Фирменный индикатор загрузки: три «искры», пульсирующие волной. Без эмодзи,
 * рисуется целиком на Canvas.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    color: Color = MaterialTheme.swiply.brand,
) {
    val transition = rememberInfiniteTransition(label = "loading")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1050, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    Box(modifier = modifier.size(width = dotSize * 5, height = dotSize * 2), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(width = dotSize * 5, height = dotSize * 2)) {
            val d = size.height * 0.5f
            val gap = d * 1.6f
            val startX = size.width / 2f - gap
            repeat(3) { i ->
                // фаза каждого шарика сдвинута — получается «волна»
                val local = ((phase - i) % 3f + 3f) % 3f
                val scale = if (local < 1f) 0.5f + 0.5f * (1f - local) else 0.5f
                drawCircle(
                    color = color.copy(alpha = 0.35f + 0.65f * ((scale - 0.5f) / 0.5f)),
                    radius = d / 2f * scale,
                    center = Offset(startX + gap * i, size.height / 2f),
                )
            }
        }
    }
}

