package com.swiply.app.feature.profile.verify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.ui.components.PrimaryButton
import com.swiply.app.core.ui.components.SecondaryButton
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.profile.ProfileRepository
import com.swiply.app.feature.profile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationUiState(
    val token: String = "",
    val isLoading: Boolean = false,
    val resent: Boolean = false,
    val done: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationUiState())
    val state: StateFlow<EmailVerificationUiState> = _state

    init {
        // сразу отправляем письмо при входе на экран
        resend()
    }

    fun onTokenChanged(value: String) = _state.update { it.copy(token = value, error = null) }

    fun resend() {
        viewModelScope.launch {
            when (val r = profileRepository.resendVerification()) {
                is AppResult.Success -> _state.update { it.copy(resent = true) }
                is AppResult.Failure -> _state.update { it.copy(error = r.error.message) }
            }
        }
    }

    fun verify() {
        val token = _state.value.token.trim()
        if (token.isEmpty()) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = profileRepository.verifyEmail(token)) {
                is AppResult.Success -> {
                    profileRepository.refreshMyProfile()
                    _state.update { it.copy(isLoading = false, done = true) }
                }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = r.error.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onBack: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.verify_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.verify_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.textSecondary,
            )
            Spacer(Modifier.height(20.dp))
            SwiplyTextField(
                value = state.token,
                onValueChange = viewModel::onTokenChanged,
                label = stringResource(R.string.verify_token),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.swiply.danger,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.verify_confirm),
                onClick = viewModel::verify,
                enabled = state.token.isNotBlank(),
                loading = state.isLoading,
            )
            Spacer(Modifier.height(10.dp))
            SecondaryButton(
                text = stringResource(R.string.verify_resend),
                onClick = viewModel::resend,
            )
            if (state.resent) {
                Text(
                    text = stringResource(R.string.verify_resent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.swiply.success,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}
