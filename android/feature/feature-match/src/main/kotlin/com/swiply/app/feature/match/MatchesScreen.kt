package com.swiply.app.feature.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.model.MatchItem
import com.swiply.app.core.ui.components.Avatar
import com.swiply.app.core.ui.components.EmptyState
import com.swiply.app.core.ui.theme.swiply
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Composable
fun MatchesScreen(
    onOpenChat: (matchId: UUID) -> Unit,
    onOpenLikes: () -> Unit,
    viewModel: MatchesViewModel = hiltViewModel(),
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val likesCount by viewModel.likesCount.collectAsStateWithLifecycle()

    // В «Сообщения» показываем только диалоги с перепиской; пустые мэтчи живут в верхней ленте
    val messageConversations = conversations.filter { it.lastMessage != null }

    // Обновляем мэтчи и счётчик лайков при каждом возврате на экран (иначе плашка врёт)
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    var unmatchTarget by remember { mutableStateOf<MatchItem?>(null) }

    unmatchTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { unmatchTarget = null },
            title = { Text(stringResource(R.string.matches_unmatch)) },
            text = { Text(stringResource(R.string.matches_unmatch_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.unmatch(target.matchId)
                        unmatchTarget = null
                    },
                ) { Text(stringResource(R.string.common_confirm), color = MaterialTheme.swiply.danger) }
            },
            dismissButton = {
                TextButton(onClick = { unmatchTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (matches.isEmpty() && messageConversations.isEmpty() && likesCount == 0) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header()
            EmptyState(
                illustration = com.swiply.app.core.ui.components.SwiplyIllustration.Matches,
                title = stringResource(R.string.matches_empty_title),
                subtitle = stringResource(R.string.matches_empty_text),
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header() }

        if (likesCount > 0) {
            item {
                LikesTeaserCard(count = likesCount, onClick = onOpenLikes)
            }
        }

        if (matches.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.matches_new),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            item {
                MatchesStrip(
                    matches = matches,
                    onClick = { onOpenChat(it.matchId) },
                    onLongClick = { unmatchTarget = it },
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.matches_messages),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        if (messageConversations.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.matches_no_messages),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.swiply.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        } else {
            items(messageConversations, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    onClick = { onOpenChat(conversation.matchId) },
                )
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

@Composable
private fun Header() {
    Text(
        text = stringResource(R.string.matches_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun LikesTeaserCard(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.swiply.brand)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(30.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.matches_likes_you, count),
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
                text = stringResource(R.string.likes_hint),
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
            )
        }
        Text(
            text = stringResource(R.string.matches_likes_you_open),
            style = MaterialTheme.typography.labelLarge,
            color = androidx.compose.ui.graphics.Color.White,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchesStrip(
    matches: List<MatchItem>,
    onClick: (MatchItem) -> Unit,
    onLongClick: (MatchItem) -> Unit,
) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(matches, key = { it.matchId }) { match ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .combinedClickable(
                        onClick = { onClick(match) },
                        onLongClick = { onLongClick(match) },
                    ),
            ) {
                Avatar(
                    url = match.thumbUrl,
                    name = match.displayName,
                    size = 64.dp,
                    isOnline = match.isOnline,
                )
                Text(
                    text = match.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            url = conversation.peer.thumbUrl,
            name = conversation.peer.displayName,
            size = 56.dp,
            isOnline = conversation.peer.isOnline,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
        ) {
            Text(
                text = conversation.peer.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
            )
            Text(
                text = conversation.lastMessage?.preview ?: stringResource(R.string.matches_no_messages),
                style = MaterialTheme.typography.bodyMedium,
                color = if (conversation.unreadCount > 0) {
                    MaterialTheme.swiply.textPrimary
                } else {
                    MaterialTheme.swiply.textSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            conversation.lastMessage?.let {
                Text(
                    text = relativeTime(it.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.swiply.textSecondary,
                )
            }
            if (conversation.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.swiply.gradientStart,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Text(conversation.unreadCount.toString())
                }
            }
        }
    }
}

@Composable
private fun relativeTime(instant: Instant): String {
    val elapsed = Duration.between(instant, Instant.now())
    return when {
        elapsed.toMinutes() < 1 -> stringResource(R.string.time_now)
        elapsed.toHours() < 1 -> stringResource(R.string.time_min, elapsed.toMinutes())
        elapsed.toDays() < 1 -> stringResource(R.string.time_hours, elapsed.toHours())
        else -> stringResource(R.string.time_days, elapsed.toDays())
    }
}
