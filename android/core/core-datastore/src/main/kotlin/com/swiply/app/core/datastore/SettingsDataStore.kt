package com.swiply.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    RUSSIAN("ru"),
    ENGLISH("en"),
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "swiply_settings")

/**
 * Пользовательские настройки: тема, язык, онбординг, уведомления.
 * Токены здесь НЕ живут — им место в [TokenStorage] (Keystore-шифрование).
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    val language: Flow<AppLanguage> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.SYSTEM
    }

    val onboardingDone: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_DONE] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setOnboardingDone() {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
