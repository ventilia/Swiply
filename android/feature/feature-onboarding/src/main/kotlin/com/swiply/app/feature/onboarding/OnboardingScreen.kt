package com.swiply.app.feature.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.theme.swiply
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
object OnboardingRoute

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    fun complete() {
        viewModelScope.launch { settingsDataStore.setOnboardingDone() }
    }
}

/**
 * Онбординг: 3 карточки с иллюстрациями на Canvas (без внешних ассетов).
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = viewModel::complete) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    color = MaterialTheme.swiply.textSecondary,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                OnboardingIllustration(
                    page = page,
                    modifier = Modifier.size(240.dp),
                )
                Spacer(Modifier.height(40.dp))
                Text(
                    text = stringResource(
                        when (page) {
                            0 -> R.string.onboarding_title_1
                            1 -> R.string.onboarding_title_2
                            else -> R.string.onboarding_title_3
                        },
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = stringResource(
                        when (page) {
                            0 -> R.string.onboarding_text_1
                            1 -> R.string.onboarding_text_2
                            else -> R.string.onboarding_text_3
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.swiply.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { index ->
                val selected = pagerState.currentPage == index
                val width by animateFloatAsState(if (selected) 28f else 8f, label = "dot-width")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .size(width = width.dp, height = 8.dp)
                        .background(
                            color = if (selected) MaterialTheme.swiply.gradientStart else MaterialTheme.swiply.outline,
                            shape = CircleShape,
                        ),
                )
            }
        }

        GradientButton(
            text = stringResource(
                if (pagerState.currentPage == 2) R.string.onboarding_start else R.string.onboarding_next,
            ),
            onClick = {
                if (pagerState.currentPage == 2) {
                    viewModel.complete()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

/** Простые векторные иллюстрации: карточки-стек, искра-мэтч, пузыри чата */
@Composable
private fun OnboardingIllustration(page: Int, modifier: Modifier = Modifier) {
    val coral = MaterialTheme.swiply.gradientStart
    val violet = MaterialTheme.swiply.gradientEnd
    val teal = MaterialTheme.swiply.superlike
    val surface = MaterialTheme.swiply.surfaceElevated

    Canvas(modifier = modifier) {
        when (page) {
            0 -> drawSwipeCards(coral, violet, surface)
            1 -> drawMatchSpark(coral, violet)
            2 -> drawChatBubbles(coral, violet, teal, surface)
        }
    }
}

private fun DrawScope.drawSwipeCards(coral: Color, violet: Color, surface: Color) {
    val cardSize = Size(size.width * 0.55f, size.height * 0.72f)
    // задние карточки
    rotate(degrees = -10f, pivot = center) {
        drawRoundRect(
            color = surface,
            topLeft = Offset(center.x - cardSize.width / 2, center.y - cardSize.height / 2),
            size = cardSize,
            cornerRadius = CornerRadius(40f, 40f),
        )
    }
    rotate(degrees = 8f, pivot = center) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(coral, violet)),
            topLeft = Offset(center.x - cardSize.width / 2, center.y - cardSize.height / 2),
            size = cardSize,
            cornerRadius = CornerRadius(40f, 40f),
        )
        // «фото» на карточке
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = cardSize.width * 0.18f,
            center = Offset(center.x, center.y - cardSize.height * 0.18f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(center.x - cardSize.width * 0.28f, center.y + cardSize.height * 0.08f),
            size = Size(cardSize.width * 0.56f, cardSize.height * 0.09f),
            cornerRadius = CornerRadius(20f, 20f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - cardSize.width * 0.22f, center.y + cardSize.height * 0.22f),
            size = Size(cardSize.width * 0.44f, cardSize.height * 0.07f),
            cornerRadius = CornerRadius(20f, 20f),
        )
    }
}

private fun DrawScope.drawMatchSpark(coral: Color, violet: Color) {
    // четырёхлучевая искра — та же, что в логотипе
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w / 2f, h * 0.08f)
        cubicTo(w * 0.56f, h * 0.38f, w * 0.62f, h * 0.44f, w * 0.92f, h / 2f)
        cubicTo(w * 0.62f, h * 0.56f, w * 0.56f, h * 0.62f, w / 2f, h * 0.92f)
        cubicTo(w * 0.44f, h * 0.62f, w * 0.38f, h * 0.56f, w * 0.08f, h / 2f)
        cubicTo(w * 0.38f, h * 0.44f, w * 0.44f, h * 0.38f, w / 2f, h * 0.08f)
        close()
    }
    drawPath(path, Brush.linearGradient(listOf(coral, violet)))
    drawCircle(coral.copy(alpha = 0.16f), radius = w * 0.46f, center = center)
    drawCircle(violet.copy(alpha = 0.10f), radius = w * 0.56f, center = center)
}

private fun DrawScope.drawChatBubbles(coral: Color, violet: Color, teal: Color, surface: Color) {
    val bubbleHeight = size.height * 0.16f
    // входящий пузырь
    drawRoundRect(
        color = surface,
        topLeft = Offset(size.width * 0.05f, size.height * 0.18f),
        size = Size(size.width * 0.58f, bubbleHeight),
        cornerRadius = CornerRadius(44f, 44f),
    )
    // исходящий градиентный
    drawRoundRect(
        brush = Brush.linearGradient(listOf(coral, violet)),
        topLeft = Offset(size.width * 0.32f, size.height * 0.44f),
        size = Size(size.width * 0.62f, bubbleHeight),
        cornerRadius = CornerRadius(44f, 44f),
    )
    // typing-индикатор
    drawRoundRect(
        color = surface,
        topLeft = Offset(size.width * 0.05f, size.height * 0.70f),
        size = Size(size.width * 0.34f, bubbleHeight * 0.85f),
        cornerRadius = CornerRadius(44f, 44f),
    )
    val dotY = size.height * 0.70f + bubbleHeight * 0.42f
    listOf(0.13f, 0.21f, 0.29f).forEachIndexed { index, x ->
        drawCircle(
            color = if (index == 1) teal else violet.copy(alpha = 0.6f),
            radius = size.width * 0.018f,
            center = Offset(size.width * x, dotY),
        )
    }
    // галочки прочитанности
    val check = Path().apply {
        moveTo(size.width * 0.80f, size.height * 0.66f)
        lineTo(size.width * 0.84f, size.height * 0.70f)
        lineTo(size.width * 0.92f, size.height * 0.60f)
    }
    drawPath(check, color = teal, style = Stroke(width = size.width * 0.015f))
}
