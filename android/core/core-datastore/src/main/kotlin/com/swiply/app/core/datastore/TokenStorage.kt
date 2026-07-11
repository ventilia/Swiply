package com.swiply.app.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
)

/**
 * Токены — только в EncryptedSharedPreferences (ключ в Android Keystore),
 * как того требует чек-лист безопасности ТЗ.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "swiply_secure",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _tokens = MutableStateFlow(read())

    /** null = не залогинен */
    val tokens: StateFlow<StoredTokens?> = _tokens

    val isLoggedIn: Boolean
        get() = _tokens.value != null

    fun accessToken(): String? = _tokens.value?.accessToken

    fun refreshToken(): String? = _tokens.value?.refreshToken

    fun userId(): String? = _tokens.value?.userId

    @Synchronized
    fun save(accessToken: String, refreshToken: String, userId: String) {
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
            putString(KEY_USER_ID, userId)
        }
        _tokens.value = StoredTokens(accessToken, refreshToken, userId)
    }

    @Synchronized
    fun clear() {
        prefs.edit { clear() }
        _tokens.value = null
    }

    private fun read(): StoredTokens? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        return StoredTokens(access, refresh, userId)
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER_ID = "user_id"
    }
}
