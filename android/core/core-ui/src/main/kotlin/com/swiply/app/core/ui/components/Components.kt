package com.swiply.app.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swiply.app.core.ui.theme.swiply

/**
 * Аватар с плейсхолдером-инициалом (сплошной бренд-цвет, v2) и опциональной
 * online-точкой.
 */
@Composable
fun Avatar(
    url: String?,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    isOnline: Boolean? = null,
) {
    Box(modifier = modifier.size(size)) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = "Фото $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.swiply.surfaceElevated),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.swiply.brand),
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }
        }
        if (isOnline == true) {
            Box(
                modifier = Modifier
                    .size(size / 4)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.swiply.online)
                    .border(2.dp, MaterialTheme.swiply.surface, CircleShape),
            )
        }
    }
}

/** Мерцающий плейсхолдер загрузки (нейтральный skeleton) */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-translate",
    )
    val base = MaterialTheme.swiply.surfaceElevated
    background(
        Brush.linearGradient(
            colors = listOf(base, base.copy(alpha = 0.4f), base),
            start = androidx.compose.ui.geometry.Offset(translate - 400f, translate - 400f),
            end = androidx.compose.ui.geometry.Offset(translate, translate),
        ),
    )
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingIndicator()
    }
}

@Composable
fun EmptyState(
    illustration: SwiplyIllustration,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SwiplyIllustrationView(illustration = illustration)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 22.dp),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (action != null) {
            Box(modifier = Modifier.padding(top = 24.dp)) { action() }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        illustration = SwiplyIllustration.Error,
        title = "Не получилось",
        subtitle = message,
        modifier = modifier,
        action = onRetry?.let { retry ->
            { PrimaryButton(text = "Повторить", onClick = retry, modifier = Modifier.padding(horizontal = 48.dp)) }
        },
    )
}
