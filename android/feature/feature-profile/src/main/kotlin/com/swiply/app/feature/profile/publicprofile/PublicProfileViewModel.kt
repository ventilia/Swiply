package com.swiply.app.feature.profile.publicprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.PublicProfile
import com.swiply.app.core.model.ReportReason
import com.swiply.app.feature.profile.ProfileRepository
import com.swiply.app.feature.profile.PublicProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PublicProfileUiState(
    val profile: PublicProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportSent: Boolean = false,
    val blocked: Boolean = false,
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val userId: UUID = UUID.fromString(savedStateHandle.toRoute<PublicProfileRoute>().userId)

    private val _state = MutableStateFlow(PublicProfileUiState())
    val state: StateFlow<PublicProfileUiState> = _state

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.publicProfile(userId)) {
                is AppResult.Success -> _state.update { it.copy(profile = result.data, isLoading = false) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }

    fun block() {
        viewModelScope.launch {
            when (val result = profileRepository.blockUser(userId)) {
                is AppResult.Success -> _state.update { it.copy(blocked = true) }
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    fun report(reason: ReportReason, description: String?) {
        viewModelScope.launch {
            when (val result = profileRepository.reportUser(userId, reason, description)) {
                is AppResult.Success -> _state.update { it.copy(reportSent = true) }
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }
}
