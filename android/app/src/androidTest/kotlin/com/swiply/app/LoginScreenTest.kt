package com.swiply.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.swiply.app.core.ui.theme.SwiplyTheme
import com.swiply.app.feature.auth.AuthRepository
import com.swiply.app.feature.auth.login.LoginScreen
import com.swiply.app.feature.auth.login.LoginViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI-тест ключевого сценария входа (запускается на эмуляторе:
 * ./gradlew :app:connectedDebugAndroidTest).
 */
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(viewModel: LoginViewModel) {
        composeRule.setContent {
            SwiplyTheme {
                LoginScreen(
                    onNavigateToRegister = {},
                    onNavigateToForgotPassword = {},
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun submitDisabledUntilFormValid() {
        val viewModel = LoginViewModel(mockk<AuthRepository>(relaxed = true))
        setContent(viewModel)

        composeRule.onNodeWithTag("login_submit").assertIsNotEnabled()

        composeRule.onNodeWithTag("login_email").performTextInput("user@test.dev")
        composeRule.onNodeWithTag("login_submit").assertIsNotEnabled()

        composeRule.onNodeWithTag("login_password").performTextInput("password123")
        composeRule.onNodeWithTag("login_submit").assertIsEnabled()
    }

    @Test
    fun typingUpdatesState() {
        val viewModel = LoginViewModel(mockk<AuthRepository>(relaxed = true))
        setContent(viewModel)

        composeRule.onNodeWithTag("login_email").performTextInput("anna@swiply.io")

        assert(viewModel.state.value.email == "anna@swiply.io")
    }
}
