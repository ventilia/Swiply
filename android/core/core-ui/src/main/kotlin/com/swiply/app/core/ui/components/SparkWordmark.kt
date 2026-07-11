package com.swiply.app.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiply.app.core.ui.theme.Manrope
import com.swiply.app.core.ui.theme.swiply

/**
 * Словесный знак «swiply»: Manrope ExtraBold сплошным бренд-цветом (v2, без
 * градиента), искра вместо точки над i.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SparkWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 44.sp,
) {
    val brand = MaterialTheme.swiply.brand
    val sparkColor = MaterialTheme.swiply.brand
    Box(modifier = modifier) {
        Text(
            text = "swiply",
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                letterSpacing = (-1).sp,
                color = brand,
            ),
        )
        // Искра над «i» (3-я буква): позиция подобрана относительно кегля
        val sparkSize = with(androidx.compose.ui.platform.LocalDensity.current) { (fontSize.toDp() * 0.30f) }
        Canvas(
            modifier = Modifier
                .size(sparkSize)
                .offset(
                    x = with(androidx.compose.ui.platform.LocalDensity.current) { (fontSize.toDp() * 1.52f) },
                    y = with(androidx.compose.ui.platform.LocalDensity.current) { (fontSize.toDp() * -0.12f) },
                ),
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                cubicTo(w * 0.56f, h * 0.36f, w * 0.64f, h * 0.44f, w, h / 2f)
                cubicTo(w * 0.64f, h * 0.56f, w * 0.56f, h * 0.64f, w / 2f, h)
                cubicTo(w * 0.44f, h * 0.64f, w * 0.36f, h * 0.56f, 0f, h / 2f)
                cubicTo(w * 0.36f, h * 0.44f, w * 0.44f, h * 0.36f, w / 2f, 0f)
                close()
            }
            drawPath(path, sparkColor)
            drawCircle(sparkColor.copy(alpha = 0.35f), radius = w * 0.7f, center = Offset(w / 2f, h / 2f))
        }
    }
}
