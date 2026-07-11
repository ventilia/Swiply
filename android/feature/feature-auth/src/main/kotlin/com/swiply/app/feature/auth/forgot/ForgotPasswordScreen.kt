package com.swiply.app.feature.auth.forgot

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.auth.R

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.resetDone) {
        if (state.resetDone) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.auth_back),
            )
        }
        Text(
            text = stringResource(R.string.auth_forgot_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.auth_forgot_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.swiply.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        SwiplyTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        GradientButton(
            text = stringResource(R.string.auth_forgot_send),
            onClick = viewModel::sendEmail,
            enabled = state.email.contains("@"),
            loading = state.isLoading && !state.emailSent,
        )

        if (state.emailSent) {
            Text(
                text = stringResource(R.string.auth_forgot_sent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.success,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
            )
            SwiplyTextField(
                value = state.token,
                onValueChange = viewModel::onTokenChanged,
                label = stringResource(R.string.auth_reset_token),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            SwiplyTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                label = stringResource(R.string.auth_new_password),
                isPassword = true,
                supportingText = stringResource(R.string.auth_password_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            GradientButton(
                text = stringResource(R.string.auth_reset_button),
                onClick = viewModel::resetPassword,
                enabled = state.token.isNotBlank() && state.newPassword.length >= 8,
                loading = state.isLoading && state.emailSent,
            )
        }

        if (state.error != null) {
            Text(
                text = state.error.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.danger,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
