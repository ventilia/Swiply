package com.swiply.app.feature.discovery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.model.MatchCelebration
import com.swiply.app.core.model.SwipeAction
import com.swiply.app.core.ui.components.EmptyState
import com.swiply.app.core.ui.components.ErrorState
import com.swiply.app.core.ui.components.LoadingIndicator
import com.swiply.app.core.ui.components.PrimaryButton
import com.swiply.app.core.ui.components.SparkWordmark
import com.swiply.app.core.ui.components.SwiplyIllustration
import com.swiply.app.core.ui.theme.CardShape
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.discovery.components.SwipeCardStack
import com.swiply.app.feature.discovery.components.rememberSwipeDeckController
import java.util.UUID

@Composable
fun DiscoveryScreen(
    onOpenProfile: (UUID) -> Unit,
    onMatch: (MatchCelebration) -> Unit,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val controller = rememberSwipeDeckController()
    val context = LocalContext.current

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        viewModel.onLocationPermissionResult(grants.values.any { it })
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* результат не критичен: доставка всё равно живёт через inbox */ }

    fun requestLocation() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onLocationPermissionResult(true)
        } else {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    // Лента грузится сразу; гео и уведомления запрашиваем параллельно, не блокируя её
    LaunchedEffect(Unit) {
        viewModel.start()
        requestLocation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DiscoveryEvent.MatchCreated -> onMatch(event.celebration)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SparkWordmark(fontSize = 28.sp)
            state.remainingLikes?.let { remaining ->
                Text(
                    text = stringResource(R.string.discovery_likes_left, remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.swiply.textSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            when {
                state.needsLocation -> EmptyState(
                    illustration = SwiplyIllustration.Location,
                    title = stringResource(R.string.discovery_permission_title),
                    subtitle = stringResource(R.string.discovery_permission_text),
                    action = {
                        PrimaryButton(
                            text = stringResource(R.string.discovery_permission_button),
                            onClick = { requestLocation() },
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    },
                )

                state.isLoading && state.deck.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CardShape)
                        .background(MaterialTheme.swiply.surfaceElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }

                state.error != null && state.deck.isEmpty() -> ErrorState(
                    message = state.error.orEmpty(),
                    onRetry = viewModel::retry,
                )

                state.deck.isEmpty() -> EmptyState(
                    illustration = SwiplyIllustration.Discovery,
                    title = stringResource(R.string.discovery_empty_title),
                    subtitle = stringResource(R.string.discovery_empty_text),
                    action = {
                        PrimaryButton(
                            text = stringResource(R.string.discovery_retry),
                            onClick = viewModel::retry,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    },
                )

                else -> SwipeCardStack(
                    deck = state.deck,
                    controller = controller,
                    onSwiped = viewModel::onSwiped,
                    onCardClick = { onOpenProfile(it.userId) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Кнопки действий под стеком
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionCircleButton(
                icon = { Icon(Icons.Filled.Replay, stringResource(R.string.discovery_btn_undo)) },
                size = 48.dp,
                tint = MaterialTheme.swiply.textSecondary,
                enabled = state.canUndo,
                onClick = viewModel::undo,
            )
            ActionCircleButton(
                icon = { Icon(Icons.Filled.Close, stringResource(R.string.discovery_btn_nope)) },
                size = 62.dp,
                tint = MaterialTheme.swiply.nope,
                enabled = state.deck.isNotEmpty(),
                onClick = { controller.swipe(SwipeAction.DISLIKE) },
            )
            ActionCircleButton(
                icon = { Icon(Icons.Filled.Star, stringResource(R.string.discovery_btn_superlike)) },
                size = 48.dp,
                tint = MaterialTheme.swiply.superlike,
                enabled = state.deck.isNotEmpty(),
                onClick = { controller.swipe(SwipeAction.SUPERLIKE) },
            )
            ActionCircleButton(
                icon = { Icon(Icons.Filled.Favorite, stringResource(R.string.discovery_btn_like)) },
                size = 62.dp,
                tint = MaterialTheme.swiply.like,
                enabled = state.deck.isNotEmpty(),
                onClick = { controller.swipe(SwipeAction.LIKE) },
            )
        }

        if (state.rateLimited) {
            Snackbar(
                modifier = Modifier.padding(12.dp),
                action = {
                    Text(
                        text = "OK",
                        color = MaterialTheme.swiply.brand,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { viewModel.dismissRateLimit() },
                    )
                },
            ) { Text(stringResource(R.string.discovery_rate_limited)) }
        }
    }
}

@Composable
private fun ActionCircleButton(
    icon: @Composable () -> Unit,
    size: Dp,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.swiply.surface, CircleShape)
            .border(1.5.dp, tint.copy(alpha = if (enabled) 0.7f else 0.25f), CircleShape),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint.copy(alpha = if (enabled) 1f else 0.35f),
        ) {
            icon()
        }
    }
}
