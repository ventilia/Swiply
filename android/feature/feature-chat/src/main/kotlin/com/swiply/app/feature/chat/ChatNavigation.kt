package com.swiply.app.feature.chat

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class ChatRoute(val matchId: String)

fun NavGraphBuilder.chatScreen(
    onBack: () -> Unit,
    onOpenProfile: (UUID) -> Unit,
) {
    composable<ChatRoute> {
        ChatScreen(
            onBack = onBack,
            onOpenProfile = onOpenProfile,
        )
    }
}
