package com.swiply.app.feature.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppResult
import com.swiply.app.feature.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ForgotPasswordUiState(
    val email: String = "",
    val token: String = "",
    val newPassword: String = "",
    val emailSent: Boolean = false,
    val resetDone: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onTokenChanged(value: String) = _state.update { it.copy(token = value, error = null) }
    fun onNewPasswordChanged(value: String) = _state.update { it.copy(newPassword = value, error = null) }

    fun sendEmail() {
        val email = _state.value.email
        if (!email.contains("@")) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(email)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, emailSent = true) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }

    fun resetPassword() {
        val s = _state.value
        if (s.token.isBlank() || s.newPassword.length < 8) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.resetPassword(s.token, s.newPassword)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, resetDone = true) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }
}
