package com.swiply.app.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

@Serializable
object BlockedUsersRoute

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onOpenBlocked: () -> Unit,
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onBack = onBack,
            onOpenBlocked = onOpenBlocked,
        )
    }
}

fun NavGraphBuilder.blockedUsersScreen(onBack: () -> Unit) {
    composable<BlockedUsersRoute> {
        BlockedUsersScreen(onBack = onBack)
    }
}
