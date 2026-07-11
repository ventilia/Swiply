package com.swiply.app.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.components.SparkWordmark
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.auth.R

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))
        SparkWordmark()
        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.auth_login_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.auth_login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.swiply.textSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 24.dp),
        )

        SwiplyTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email"),
        )
        Spacer(Modifier.height(12.dp))
        SwiplyTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = stringResource(R.string.auth_password),
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password"),
        )

        if (state.error != null) {
            Text(
                text = state.error.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    color = MaterialTheme.swiply.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        GradientButton(
            text = stringResource(R.string.auth_login_button),
            onClick = viewModel::submit,
            enabled = state.canSubmit,
            loading = state.isLoading,
            modifier = Modifier.testTag("login_submit"),
        )

        Row(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.auth_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.textSecondary,
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = stringResource(R.string.auth_register_link),
                    color = MaterialTheme.swiply.gradientStart,
                )
            }
        }
    }
}
