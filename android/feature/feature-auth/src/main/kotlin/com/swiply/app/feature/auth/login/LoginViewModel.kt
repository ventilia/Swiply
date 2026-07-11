package com.swiply.app.feature.auth.login

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = email.contains("@") && password.length >= 8 && !isLoading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChanged(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(current.email, current.password)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false) }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.error.message)
                }
            }
        }
    }
}
