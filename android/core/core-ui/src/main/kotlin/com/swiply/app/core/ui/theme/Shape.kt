package com.swiply.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Форма из ТЗ: карточки 28–32dp, кнопки 16–20dp, чипы — pill.
 */
val SwiplyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val CardShape = RoundedCornerShape(28.dp)
val ButtonShape = RoundedCornerShape(18.dp)
val PillShape = RoundedCornerShape(percent = 50)
