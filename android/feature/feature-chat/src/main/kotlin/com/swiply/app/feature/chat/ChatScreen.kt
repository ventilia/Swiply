package com.swiply.app.feature.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.MessageStatus
import com.swiply.app.core.model.MessageType
import com.swiply.app.core.ui.components.Avatar
import com.swiply.app.core.ui.theme.swiply
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onOpenProfile: (java.util.UUID) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val input by viewModel.input.collectAsStateWithLifecycle()
    val messages = viewModel.messages.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::sendImage) }

    // при новом сообщении (низ списка в reverseLayout — index 0) скроллим вниз
    LaunchedEffect(messages.itemCount) {
        if (messages.itemCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val peer = conversation?.peer
                    if (peer != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Avatar(url = peer.thumbUrl, name = peer.displayName, size = 38.dp, isOnline = peer.isOnline)
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(peer.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = when {
                                        input.peerTyping -> stringResource(R.string.chat_typing)
                                        peer.isOnline -> stringResource(R.string.chat_online)
                                        else -> stringResource(R.string.chat_last_seen)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (input.peerTyping || peer.isOnline) {
                                        MaterialTheme.swiply.online
                                    } else {
                                        MaterialTheme.swiply.textSecondary
                                    },
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.chat_back),
                        )
                    }
                },
                modifier = Modifier.let { m ->
                    val peer = conversation?.peer
                    if (peer != null) {
                        m.clickableNoRipple { onOpenProfile(peer.userId) }
                    } else {
                        m
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (messages.itemCount == 0 && messages.loadState.refresh is LoadState.NotLoading) {
                    EmptyChatState(onIcebreaker = { text ->
                        viewModel.onTextChanged(text)
                        viewModel.sendText()
                    })
                }
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 10.dp,
                    ),
                ) {
                    if (input.peerTyping) {
                        item(key = "typing") { TypingBubble() }
                    }
                    items(
                        count = messages.itemCount,
                        key = messages.itemKey { it.id },
                    ) { index ->
                        val message = messages[index] ?: return@items
                        MessageBubble(
                            message = message,
                            isMine = message.senderId == viewModel.myUserId,
                        )
                    }
                }
            }

            input.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.swiply.danger,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // ===== Ввод =====
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                IconButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = !input.isSendingImage,
                ) {
                    if (input.isSendingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Filled.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.chat_attach),
                            tint = MaterialTheme.swiply.textSecondary,
                        )
                    }
                }
                OutlinedTextField(
                    value = input.text,
                    onValueChange = viewModel::onTextChanged,
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.swiply.gradientEnd,
                        cursorColor = MaterialTheme.swiply.gradientEnd,
                    ),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = viewModel::sendText,
                    enabled = input.text.isNotBlank(),
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(48.dp)
                        .background(
                            brush = MaterialTheme.swiply.brandGradientHorizontal,
                            shape = CircleShape,
                            alpha = if (input.text.isNotBlank()) 1f else 0.4f,
                        ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@Composable
private fun MessageBubble(message: ChatMessage, isMine: Boolean) {
    val bubbleShape = if (isMine) {
        RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .then(
                    if (isMine) {
                        Modifier.background(MaterialTheme.swiply.brandGradientHorizontal)
                    } else {
                        Modifier.background(MaterialTheme.swiply.surfaceElevated)
                    },
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) {
            when (message.type) {
                MessageType.IMAGE -> AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = stringResource(R.string.chat_photo_message),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
                else -> Text(
                    text = message.content.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isMine) Color.White else MaterialTheme.swiply.textPrimary,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp),
            ) {
                Text(
                    text = message.sentAt.atZone(ZoneId.systemDefault()).format(TIME_FORMAT),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMine) Color.White.copy(alpha = 0.75f) else MaterialTheme.swiply.textSecondary,
                )
                if (isMine) {
                    val (icon, tint) = when {
                        message.isPending -> Icons.Filled.Done to Color.White.copy(alpha = 0.45f)
                        message.status == MessageStatus.READ -> Icons.Filled.DoneAll to Color.White
                        message.status == MessageStatus.DELIVERED -> Icons.Filled.DoneAll to Color.White.copy(alpha = 0.6f)
                        else -> Icons.Filled.Done to Color.White.copy(alpha = 0.6f)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = message.status.name,
                        tint = tint,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(15.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingBubble() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "typing-alpha",
    )
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp))
                .background(MaterialTheme.swiply.surfaceElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                MaterialTheme.swiply.textSecondary.copy(alpha = alpha),
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(onIcebreaker: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        com.swiply.app.core.ui.components.SwiplyIllustrationView(
            illustration = com.swiply.app.core.ui.components.SwiplyIllustration.Chat,
        )
        Text(
            text = stringResource(R.string.chat_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.chat_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.swiply.textSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
        )
        // Icebreakers — стартовые фразы (бонус из ТЗ)
        listOf(
            stringResource(R.string.chat_icebreaker_1),
            stringResource(R.string.chat_icebreaker_2),
            stringResource(R.string.chat_icebreaker_3),
        ).forEach { phrase ->
            SuggestionChip(
                onClick = { onIcebreaker(phrase) },
                label = { Text(phrase) },
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
    }
}

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
