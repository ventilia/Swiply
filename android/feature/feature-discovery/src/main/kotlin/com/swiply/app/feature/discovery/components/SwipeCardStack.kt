package com.swiply.app.feature.discovery.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiply.app.core.model.Candidate
import com.swiply.app.core.model.SwipeAction
import com.swiply.app.core.ui.theme.CardShape
import com.swiply.app.core.ui.theme.swiply
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Контроллер программных свайпов: кнопки под стеком дёргают его,
 * верхняя карточка анимируется так же, как при живом драге.
 */
class SwipeDeckController {
    internal var pendingAction by mutableStateOf<SwipeAction?>(null)

    fun swipe(action: SwipeAction) {
        if (pendingAction == null) pendingAction = action
    }
}

@Composable
fun rememberSwipeDeckController(): SwipeDeckController = remember { SwipeDeckController() }

/**
 * Свайп-стек: drag + rotation, оверлей-штампы с прозрачностью от прогресса,
 * подложенные карточки подъезжают с анимацией масштаба.
 */
@Composable
fun SwipeCardStack(
    deck: List<Candidate>,
    controller: SwipeDeckController,
    onSwiped: (Candidate, SwipeAction) -> Unit,
    onCardClick: (Candidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = deck.take(3)

    Box(modifier = modifier) {
        // рисуем с хвоста, чтобы верхняя карточка была последней (поверх всех)
        visible.reversed().forEachIndexed { reversedIndex, candidate ->
            val depth = visible.lastIndex - reversedIndex // 0 = верхняя
            if (depth == 0) {
                TopCard(
                    // ключ по кандидату: свежая карточка получает чистое состояние драга
                    candidate = candidate,
                    controller = controller,
                    onSwiped = onSwiped,
                    onClick = { onCardClick(candidate) },
                )
            } else {
                UnderCard(candidate = candidate, depth = depth)
            }
        }
    }
}

@Composable
private fun UnderCard(candidate: Candidate, depth: Int) {
    val scale by animateFloatAsState(1f - depth * 0.04f, spring(), label = "under-scale")
    val offsetY by animateFloatAsState(depth * 26f, spring(), label = "under-offset")
    CandidateCardContent(
        candidate = candidate,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = offsetY
            },
    )
}

@Composable
private fun TopCard(
    candidate: Candidate,
    controller: SwipeDeckController,
    onSwiped: (Candidate, SwipeAction) -> Unit,
    onClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 110.dp.toPx() }
    val superThresholdPx = with(density) { 130.dp.toPx() }
    val flingDistancePx = with(density) { 640.dp.toPx() }

    // ключ = кандидат: у новой верхней карточки offset обнулён
    val offset = remember(candidate.userId) {
        Animatable(Offset.Zero, Offset.VectorConverter)
    }

    val progressX = (offset.value.x / swipeThresholdPx).coerceIn(-1.5f, 1.5f)
    val progressUp = (-offset.value.y / superThresholdPx).coerceIn(0f, 1.5f)

    suspend fun flingAway(action: SwipeAction) {
        val target = when (action) {
            SwipeAction.LIKE -> Offset(flingDistancePx, offset.value.y * 1.6f)
            SwipeAction.DISLIKE -> Offset(-flingDistancePx, offset.value.y * 1.6f)
            SwipeAction.SUPERLIKE -> Offset(offset.value.x * 1.4f, -flingDistancePx)
        }
        offset.animateTo(target, tween(durationMillis = 260))
        onSwiped(candidate, action)
    }

    // программный свайп от кнопок
    LaunchedEffect(controller.pendingAction, candidate.userId) {
        val action = controller.pendingAction ?: return@LaunchedEffect
        flingAway(action)
        controller.pendingAction = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offset.value.x
                translationY = offset.value.y
                rotationZ = (offset.value.x / 60f).coerceIn(-12f, 12f)
            }
            .pointerInput(candidate.userId) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offset.snapTo(offset.value + dragAmount) }
                    },
                    onDragEnd = {
                        scope.launch {
                            val value = offset.value
                            when {
                                value.x > swipeThresholdPx -> flingAway(SwipeAction.LIKE)
                                value.x < -swipeThresholdPx -> flingAway(SwipeAction.DISLIKE)
                                value.y < -superThresholdPx && abs(value.x) < swipeThresholdPx ->
                                    flingAway(SwipeAction.SUPERLIKE)
                                else -> offset.animateTo(Offset.Zero, spring(stiffness = 350f))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offset.animateTo(Offset.Zero, spring(stiffness = 350f)) }
                    },
                )
            },
    ) {
        CandidateCardContent(
            candidate = candidate,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
        )

        // Штампы: прозрачность растёт с прогрессом драга
        SwipeStamp(
            text = "LIKE",
            color = MaterialTheme.swiply.like,
            alpha = progressX.coerceIn(0f, 1f),
            rotation = -14f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(28.dp),
        )
        SwipeStamp(
            text = "NOPE",
            color = MaterialTheme.swiply.nope,
            alpha = (-progressX).coerceIn(0f, 1f),
            rotation = 14f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(28.dp),
        )
        SwipeStamp(
            text = "SUPER",
            color = MaterialTheme.swiply.superlike,
            alpha = progressUp.coerceIn(0f, 1f),
            rotation = 0f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(48.dp),
        )
    }
}

@Composable
private fun SwipeStamp(
    text: String,
    color: Color,
    alpha: Float,
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontSize = 34.sp,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                rotationZ = rotation
            }
            .border(width = 4.dp, color = color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 2.dp),
    )
}

/** Содержимое карточки: фото, градиент, имя/возраст/город, бейджи */
@Composable
private fun CandidateCardContent(
    candidate: Candidate,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clip(CardShape)) {
        val photo = candidate.photos.firstOrNull()
        if (photo != null) {
            AsyncImage(
                model = photo.url,
                contentDescription = "Фото ${candidate.displayName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.swiply.surfaceElevated),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.swiply.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = candidate.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }

        // нижний градиент для читаемости текста
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${candidate.displayName}, ${candidate.age}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                if (candidate.isVerified) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.swiply.superlike,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp),
                    )
                }
                if (candidate.isOnline) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(10.dp)
                            .background(MaterialTheme.swiply.online, CircleShape),
                    )
                }
            }
            Text(
                text = listOfNotNull(candidate.city, "~${candidate.distanceKm} км").joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            candidate.bio?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (candidate.interests.isNotEmpty()) {
                com.swiply.app.core.ui.components.InterestChipsRow(
                    interests = candidate.interests.take(4),
                    onSurface = true,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}
