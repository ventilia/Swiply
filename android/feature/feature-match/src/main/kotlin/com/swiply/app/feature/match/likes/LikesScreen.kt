package com.swiply.app.feature.match.likes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.LikeReceived
import com.swiply.app.core.ui.components.EmptyState
import com.swiply.app.core.ui.components.FullScreenLoading
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.match.MatchRepository
import com.swiply.app.feature.match.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikesUiState(
    val likes: List<LikeReceived> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LikesUiState())
    val state: StateFlow<LikesUiState> = _state

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = matchRepository.likesReceived()) {
                is AppResult.Success -> _state.update {
                    it.copy(likes = result.data.items, isLoading = false, error = null)
                }
                // не проваливаемся в тихую пустоту — показываем ошибку с «Повторить»
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.error.message)
                }
            }
        }
    }
}

/**
 * «Кто лайкнул тебя»: блюр-превью до взаимного лайка — классический дейтинг-крючок.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesScreen(
    onBack: () -> Unit,
    viewModel: LikesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.likes_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading(Modifier.padding(padding))
            state.error != null -> com.swiply.app.core.ui.components.ErrorState(
                message = state.error.orEmpty(),
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            state.likes.isEmpty() -> EmptyState(
                illustration = com.swiply.app.core.ui.components.SwiplyIllustration.Likes,
                title = stringResource(R.string.likes_empty_title),
                subtitle = stringResource(R.string.likes_empty_text),
                modifier = Modifier.padding(padding),
            )
            else -> Column(Modifier.padding(padding)) {
                Text(
                    text = stringResource(R.string.likes_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.swiply.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.likes, key = { it.userId }) { like ->
                        BlurredLikeCard(like)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlurredLikeCard(like: LikeReceived) {
    Box(
        modifier = Modifier
            .aspectRatio(0.78f)
            .clip(MaterialTheme.shapes.large),
    ) {
        if (like.thumbUrl != null) {
            AsyncImage(
                model = like.thumbUrl,
                contentDescription = stringResource(R.string.likes_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(18.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.swiply.brandGradient)
                    .blur(18.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))),
                )
                .padding(12.dp),
        ) {
            Column {
                if (like.superlike) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.swiply.superlike,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.likes_superlike_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.swiply.superlike,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                Text(
                    text = "${like.displayName.first()}…, ${like.age}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }
    }
}
