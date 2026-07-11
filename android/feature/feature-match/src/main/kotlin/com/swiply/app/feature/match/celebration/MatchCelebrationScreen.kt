package com.swiply.app.feature.match.celebration

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swiply.app.core.ui.components.Avatar
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.theme.SwiplyPalette
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.match.R
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val size: Float,
    val sway: Float,
    val rotation: Float,
    val color: Color,
)

/**
 * Полноэкранный match celebration: кастомное конфетти на Canvas,
 * аватары пары и CTA «Написать» / «Продолжить смотреть».
 */
@Composable
fun MatchCelebrationScreen(
    displayName: String,
    thumbUrl: String?,
    onWriteMessage: () -> Unit,
    onContinue: () -> Unit,
) {
    val particles = remember {
        val palette = listOf(
            SwiplyPalette.Rose,
            SwiplyPalette.Violet,
            SwiplyPalette.Teal,
            SwiplyPalette.Amber,
            Color.White,
        )
        List(90) {
            ConfettiParticle(
                x = Random.nextFloat(),
                delay = Random.nextFloat(),
                speed = 0.6f + Random.nextFloat() * 0.8f,
                size = 8f + Random.nextFloat() * 14f,
                sway = 20f + Random.nextFloat() * 50f,
                rotation = Random.nextFloat() * 360f,
                color = palette[Random.nextInt(palette.size)],
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "confetti-progress",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(700),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.swiply.background),
    ) {
        // Конфетти позади контента
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val t = ((progress * particle.speed + particle.delay) % 1f)
                val y = t * (size.height + 200f) - 100f
                val x = particle.x * size.width + sin(t * 14f) * particle.sway
                rotate(particle.rotation + t * 540f, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color.copy(alpha = 0.85f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.55f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.celebration_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.swiply.gradientStart,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                },
            )
            Text(
                text = stringResource(R.string.celebration_subtitle, displayName),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.swiply.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )

            Row(
                modifier = Modifier.padding(vertical = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    url = null,
                    name = "Я",
                    size = 108.dp,
                    modifier = Modifier
                        .offset(x = 14.dp)
                        .graphicsLayer { rotationZ = -8f },
                )
                Avatar(
                    url = thumbUrl,
                    name = displayName,
                    size = 108.dp,
                    modifier = Modifier
                        .offset(x = (-14).dp)
                        .graphicsLayer { rotationZ = 8f },
                )
            }

            GradientButton(
                text = stringResource(R.string.celebration_write),
                onClick = onWriteMessage,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onContinue) {
                Text(
                    text = stringResource(R.string.celebration_continue),
                    color = MaterialTheme.swiply.textSecondary,
                )
            }
        }
    }
}
