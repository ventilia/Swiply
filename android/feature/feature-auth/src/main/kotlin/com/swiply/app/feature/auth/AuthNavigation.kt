package com.swiply.app.feature.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.swiply.app.feature.auth.forgot.ForgotPasswordScreen
import com.swiply.app.feature.auth.login.LoginScreen
import com.swiply.app.feature.auth.register.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable
object AuthGraphRoute

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object ForgotPasswordRoute


fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AuthGraphRoute>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
