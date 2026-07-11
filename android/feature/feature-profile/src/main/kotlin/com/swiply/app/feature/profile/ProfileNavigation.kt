package com.swiply.app.feature.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swiply.app.feature.profile.edit.EditProfileScreen
import com.swiply.app.feature.profile.my.MyProfileScreen
import com.swiply.app.feature.profile.publicprofile.PublicProfileScreen
import com.swiply.app.feature.profile.verify.EmailVerificationScreen
import kotlinx.serialization.Serializable

@Serializable
object MyProfileRoute

@Serializable
object EditProfileRoute

@Serializable
object EmailVerificationRoute

@Serializable
data class PublicProfileRoute(val userId: String)

fun NavGraphBuilder.myProfileScreen(
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onVerifyEmail: () -> Unit,
) {
    composable<MyProfileRoute> {
        MyProfileScreen(
            onEditProfile = onEditProfile,
            onOpenSettings = onOpenSettings,
            onVerifyEmail = onVerifyEmail,
        )
    }
}

fun NavGraphBuilder.emailVerificationScreen(onBack: () -> Unit) {
    composable<EmailVerificationRoute> {
        EmailVerificationScreen(onBack = onBack)
    }
}

fun NavGraphBuilder.editProfileScreen(onBack: () -> Unit) {
    composable<EditProfileRoute> {
        EditProfileScreen(onBack = onBack)
    }
}

fun NavGraphBuilder.publicProfileScreen(onBack: () -> Unit) {
    composable<PublicProfileRoute> {
        PublicProfileScreen(onBack = onBack)
    }
}
