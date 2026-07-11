package com.swiply.app.feature.match

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.swiply.app.feature.match.celebration.MatchCelebrationScreen
import com.swiply.app.feature.match.likes.LikesScreen
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
object MatchesRoute

@Serializable
object LikesRoute

@Serializable
data class MatchCelebrationRoute(
    val matchId: String,
    val displayName: String,
    val thumbUrl: String? = null,
)

fun NavGraphBuilder.matchesScreen(
    onOpenChat: (matchId: UUID) -> Unit,
    onOpenLikes: () -> Unit,
) {
    composable<MatchesRoute> {
        MatchesScreen(
            onOpenChat = onOpenChat,
            onOpenLikes = onOpenLikes,
        )
    }
}

fun NavGraphBuilder.likesScreen(onBack: () -> Unit) {
    composable<LikesRoute> {
        LikesScreen(onBack = onBack)
    }
}

fun NavGraphBuilder.matchCelebrationScreen(
    onWriteMessage: (matchId: UUID) -> Unit,
    onContinue: () -> Unit,
) {
    composable<MatchCelebrationRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MatchCelebrationRoute>()
        MatchCelebrationScreen(
            displayName = route.displayName,
            thumbUrl = route.thumbUrl,
            onWriteMessage = { onWriteMessage(UUID.fromString(route.matchId)) },
            onContinue = onContinue,
        )
    }
}
