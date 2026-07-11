package com.swiply.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.swiply.app.core.datastore.ThemeMode
import com.swiply.app.core.network.SessionState
import com.swiply.app.core.ui.components.FullScreenLoading
import com.swiply.app.core.ui.theme.SwiplyTheme
import com.swiply.app.feature.auth.AuthGraphRoute
import com.swiply.app.feature.auth.authGraph
import com.swiply.app.feature.onboarding.OnboardingScreen

/**
 * Корень: тема из DataStore, реактивное переключение
 * Onboarding → Auth → Main по состоянию сессии.
 */
@Composable
fun SwiplyRoot(viewModel: RootViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    SwiplyTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                onboardingDone == null -> FullScreenLoading()
                session is SessionState.LoggedIn -> MainScaffold()
                onboardingDone == false -> OnboardingScreen()
                else -> AuthNavHost()
            }
        }
    }
}

@Composable
private fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AuthGraphRoute) {
        authGraph(navController)
    }
}
