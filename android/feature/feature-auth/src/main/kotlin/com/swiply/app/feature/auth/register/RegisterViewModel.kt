package com.swiply.app.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.datastore.AppLanguage
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.Interests
import com.swiply.app.feature.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val interestedIn: Set<Gender> = emptySet(),
    val interests: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val underage: Boolean = false,
) {
    val emailValid: Boolean get() = email.contains("@") && email.length >= 5
    val passwordValid: Boolean get() = password.length >= 8
    val nameValid: Boolean get() = displayName.trim().length >= 2

    val canSubmit: Boolean
        get() = emailValid && passwordValid && nameValid &&
            birthDate != null && !underage &&
            gender != null && interestedIn.isNotEmpty() && !isLoading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state


    val language: StateFlow<AppLanguage> = settingsDataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.SYSTEM)

    fun onEmailChanged(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChanged(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onNameChanged(value: String) = _state.update { it.copy(displayName = value, error = null) }

    fun onBirthDateChanged(value: LocalDate) = _state.update {
        val age = Period.between(value, LocalDate.now()).years
        it.copy(birthDate = value, underage = age < 18, error = null)
    }

    fun onGenderChanged(value: Gender) = _state.update { it.copy(gender = value, error = null) }

    fun onInterestToggled(value: Gender) = _state.update {
        val updated = if (value in it.interestedIn) it.interestedIn - value else it.interestedIn + value
        it.copy(interestedIn = updated, error = null)
    }


    fun onHobbyToggled(value: String) = _state.update {
        val updated = when {
            value in it.interests -> it.interests - value
            it.interests.size >= Interests.MAX_SELECTED -> it.interests
            else -> it.interests + value
        }
        it.copy(interests = updated, error = null)
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsDataStore.setLanguage(language) }
    }

    fun submit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.register(
                email = s.email,
                password = s.password,
                displayName = s.displayName,
                birthDate = s.birthDate!!,
                gender = s.gender!!,
                interestedIn = s.interestedIn,
                interests = s.interests.toList(),
            )
            when (result) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false) }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.error.message)
                }
            }
        }
    }
}
