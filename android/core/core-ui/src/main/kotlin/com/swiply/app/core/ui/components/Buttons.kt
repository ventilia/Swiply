package com.swiply.app.core.ui.components

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swiply.app.core.ui.theme.ButtonShape
import com.swiply.app.core.ui.theme.swiply

/**
 * Главная CTA-кнопка: сплошной бренд-цвет (v2, без градиента), с тактильным
 * затемнением при нажатии.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val container by animateColorAsState(
        targetValue = when {
            !enabled || loading -> MaterialTheme.swiply.brand.copy(alpha = 0.45f)
            pressed -> MaterialTheme.swiply.brandPressed
            else -> MaterialTheme.swiply.brand
        },
        label = "container",
    )
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = ButtonShape,
        interactionSource = interaction,
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            disabledContainerColor = MaterialTheme.swiply.brand.copy(alpha = 0.45f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

/** Вторичная кнопка: контур, без заливки. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Совместимость: раньше кнопка была градиентной. В дизайне v2 градиентов нет —
 * это тонкий псевдоним [PrimaryButton].
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) = PrimaryButton(text, onClick, modifier, enabled, loading)
