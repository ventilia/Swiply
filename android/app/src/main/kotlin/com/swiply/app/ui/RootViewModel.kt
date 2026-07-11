package com.swiply.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.datastore.ThemeMode
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
    sessionManager: SessionManager,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    /** null = ещё читаем DataStore (сплэш не мигает не тем экраном) */
    val onboardingDone: StateFlow<Boolean?> = settingsDataStore.onboardingDone
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val session: StateFlow<SessionState> = sessionManager.state
}
