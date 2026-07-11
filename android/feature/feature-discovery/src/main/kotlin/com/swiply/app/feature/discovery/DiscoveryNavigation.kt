package com.swiply.app.feature.discovery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swiply.app.core.model.MatchCelebration
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
object DiscoveryRoute

fun NavGraphBuilder.discoveryScreen(
    onOpenProfile: (UUID) -> Unit,
    onMatch: (MatchCelebration) -> Unit,
) {
    composable<DiscoveryRoute> {
        DiscoveryScreen(
            onOpenProfile = onOpenProfile,
            onMatch = onMatch,
        )
    }
}
