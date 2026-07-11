package com.swiply.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swiply.app.R
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.chat.ChatRoute
import com.swiply.app.feature.chat.chatScreen
import com.swiply.app.feature.discovery.DiscoveryRoute
import com.swiply.app.feature.discovery.discoveryScreen
import com.swiply.app.feature.match.MatchCelebrationRoute
import com.swiply.app.feature.match.LikesRoute
import com.swiply.app.feature.match.MatchesRoute
import com.swiply.app.feature.match.likesScreen
import com.swiply.app.feature.match.matchCelebrationScreen
import com.swiply.app.feature.match.matchesScreen
import com.swiply.app.feature.profile.EditProfileRoute
import com.swiply.app.feature.profile.EmailVerificationRoute
import com.swiply.app.feature.profile.MyProfileRoute
import com.swiply.app.feature.profile.PublicProfileRoute
import com.swiply.app.feature.profile.editProfileScreen
import com.swiply.app.feature.profile.emailVerificationScreen
import com.swiply.app.feature.profile.myProfileScreen
import com.swiply.app.feature.profile.publicProfileScreen
import com.swiply.app.feature.settings.BlockedUsersRoute
import com.swiply.app.feature.settings.SettingsRoute
import com.swiply.app.feature.settings.blockedUsersScreen
import com.swiply.app.feature.settings.settingsScreen

/**
 * Главный каркас: три таба (лента / мэтчи / профиль) + стек деталей.
 */
@Composable
fun MainScaffold(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val unread by viewModel.unreadTotal.collectAsStateWithLifecycle()

    // Пассивная сторона мэтча тоже видит celebration — событие приходит по WS
    LaunchedEffect(Unit) {
        viewModel.incomingMatches.collect { celebration ->
            navController.navigate(
                MatchCelebrationRoute(
                    matchId = celebration.matchId.toString(),
                    displayName = celebration.otherDisplayName,
                    thumbUrl = celebration.otherThumbUrl,
                ),
            )
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val onTab = currentDestination?.let { destination ->
        destination.hasRoute(DiscoveryRoute::class) ||
            destination.hasRoute(MatchesRoute::class) ||
            destination.hasRoute(MyProfileRoute::class)
    } ?: true

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (onTab) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.swiply.outline,
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.swiply.surface,
                        tonalElevation = 0.dp,
                    ) {
                        val itemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.swiply.brand,
                            selectedTextColor = MaterialTheme.swiply.brand,
                            unselectedIconColor = MaterialTheme.swiply.textSecondary,
                            unselectedTextColor = MaterialTheme.swiply.textSecondary,
                            indicatorColor = MaterialTheme.swiply.brandContainer,
                        )
                        // Порядок: Профиль слева · Лента в центре · Мэтчи справа
                        val profile = currentDestination?.hasRoute(MyProfileRoute::class) == true
                        NavigationBarItem(
                            selected = profile,
                            onClick = { navigateToTab(MyProfileRoute) },
                            icon = {
                                Icon(
                                    if (profile) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(R.string.nav_profile)) },
                            colors = itemColors,
                        )
                        val discover = currentDestination?.hasRoute(DiscoveryRoute::class) == true
                        NavigationBarItem(
                            selected = discover,
                            onClick = { navigateToTab(DiscoveryRoute) },
                            icon = {
                                Icon(
                                    if (discover) Icons.Filled.Whatshot else Icons.Outlined.Whatshot,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(R.string.nav_discover)) },
                            colors = itemColors,
                        )
                        val matches = currentDestination?.hasRoute(MatchesRoute::class) == true
                        NavigationBarItem(
                            selected = matches,
                            onClick = { navigateToTab(MatchesRoute) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unread > 0) {
                                            Badge(containerColor = MaterialTheme.swiply.brand) {
                                                Text(if (unread > 99) "99+" else unread.toString())
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        if (matches) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_matches)) },
                            colors = itemColors,
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DiscoveryRoute,
            modifier = Modifier.padding(padding),
        ) {
            discoveryScreen(
                onOpenProfile = { userId ->
                    navController.navigate(PublicProfileRoute(userId.toString()))
                },
                onMatch = { celebration ->
                    navController.navigate(
                        MatchCelebrationRoute(
                            matchId = celebration.matchId.toString(),
                            displayName = celebration.otherDisplayName,
                            thumbUrl = celebration.otherThumbUrl,
                        ),
                    )
                },
            )
            matchesScreen(
                onOpenChat = { matchId -> navController.navigate(ChatRoute(matchId.toString())) },
                onOpenLikes = { navController.navigate(LikesRoute) },
            )
            myProfileScreen(
                onEditProfile = { navController.navigate(EditProfileRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onVerifyEmail = { navController.navigate(EmailVerificationRoute) },
            )
            emailVerificationScreen(onBack = { navController.popBackStack() })

            likesScreen(onBack = { navController.popBackStack() })
            matchCelebrationScreen(
                onWriteMessage = { matchId ->
                    navController.popBackStack()
                    navController.navigate(ChatRoute(matchId.toString()))
                },
                onContinue = { navController.popBackStack() },
            )
            chatScreen(
                onBack = { navController.popBackStack() },
                onOpenProfile = { userId ->
                    navController.navigate(PublicProfileRoute(userId.toString()))
                },
            )
            publicProfileScreen(onBack = { navController.popBackStack() })
            editProfileScreen(onBack = { navController.popBackStack() })
            settingsScreen(
                onBack = { navController.popBackStack() },
                onOpenBlocked = { navController.navigate(BlockedUsersRoute) },
            )
            blockedUsersScreen(onBack = { navController.popBackStack() })
        }
    }
}
