package com.swiply.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.datastore.AppLanguage
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.datastore.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SettingsActionState(
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val blocked: List<BlockedUser> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val language: StateFlow<AppLanguage> = settingsDataStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.SYSTEM)

    val notificationsEnabled: StateFlow<Boolean> = settingsDataStore.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _actions = MutableStateFlow(SettingsActionState())
    val actions: StateFlow<SettingsActionState> = _actions

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsDataStore.setLanguage(language) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setNotificationsEnabled(enabled) }
    }

    fun logout() {
        viewModelScope.launch { settingsRepository.logout() }
    }

    fun deleteAccount(password: String) {
        _actions.update { it.copy(isDeleting = true, deleteError = null) }
        viewModelScope.launch {
            when (val result = settingsRepository.deleteAccount(password)) {
                is AppResult.Success -> Unit // SessionManager сам переключит граф на логин
                is AppResult.Failure -> _actions.update {
                    it.copy(isDeleting = false, deleteError = result.error.message)
                }
            }
        }
    }

    fun loadBlocked() {
        viewModelScope.launch {
            when (val result = settingsRepository.blockedUsers()) {
                is AppResult.Success -> _actions.update { it.copy(blocked = result.data) }
                is AppResult.Failure -> Unit
            }
        }
    }

    fun unblock(userId: UUID) {
        viewModelScope.launch {
            settingsRepository.unblock(userId)
            loadBlocked()
        }
    }
}
