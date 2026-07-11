package com.swiply.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.network.SessionManager
import com.swiply.app.ui.SwiplyRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Единственная Activity. AppCompatActivity (а не ComponentActivity) — ради
 * per-app locales (AppCompatDelegate.setApplicationLocales) для переключения RU/EN.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Язык приложения: RU/EN/системный
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    settingsDataStore.language.distinctUntilChanged().collect { language ->
                        val locales = language.tag
                            ?.let { LocaleListCompat.forLanguageTags(it) }
                            ?: LocaleListCompat.getEmptyLocaleList()
                        if (AppCompatDelegate.getApplicationLocales() != locales) {
                            AppCompatDelegate.setApplicationLocales(locales)
                        }
                    }
                }
                launch {
                    sessionManager.sessionExpired.collect {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.session_expired),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }

        setContent {
            SwiplyRoot()
        }
    }
}
