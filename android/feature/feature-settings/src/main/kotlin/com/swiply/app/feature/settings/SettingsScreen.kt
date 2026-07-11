package com.swiply.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.datastore.AppLanguage
import com.swiply.app.core.datastore.ThemeMode
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.swiply

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenBlocked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val theme by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete)) },
            text = {
                Column {
                    Text(stringResource(R.string.settings_delete_text))
                    Spacer(Modifier.height(12.dp))
                    SwiplyTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = stringResource(R.string.settings_delete_password),
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    actions.deleteError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.swiply.danger,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount(deletePassword) },
                    enabled = deletePassword.isNotBlank() && !actions.isDeleting,
                ) {
                    Text(stringResource(R.string.settings_delete_confirm), color = MaterialTheme.swiply.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // ===== Тема =====
            SectionTitle(stringResource(R.string.settings_appearance))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(stringResource(R.string.settings_theme_system), theme == ThemeMode.SYSTEM) {
                    viewModel.setTheme(ThemeMode.SYSTEM)
                }
                ThemeChip(stringResource(R.string.settings_theme_light), theme == ThemeMode.LIGHT) {
                    viewModel.setTheme(ThemeMode.LIGHT)
                }
                ThemeChip(stringResource(R.string.settings_theme_dark), theme == ThemeMode.DARK) {
                    viewModel.setTheme(ThemeMode.DARK)
                }
            }

            // ===== Язык =====
            SectionTitle(stringResource(R.string.settings_language))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(stringResource(R.string.settings_lang_system), language == AppLanguage.SYSTEM) {
                    viewModel.setLanguage(AppLanguage.SYSTEM)
                }
                ThemeChip(stringResource(R.string.settings_lang_ru), language == AppLanguage.RUSSIAN) {
                    viewModel.setLanguage(AppLanguage.RUSSIAN)
                }
                ThemeChip(stringResource(R.string.settings_lang_en), language == AppLanguage.ENGLISH) {
                    viewModel.setLanguage(AppLanguage.ENGLISH)
                }
            }

            // ===== Уведомления =====
            SectionTitle(stringResource(R.string.settings_notifications))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.swiply.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = notifications, onCheckedChange = viewModel::setNotifications)
            }

            // ===== Аккаунт =====
            SectionTitle(stringResource(R.string.settings_account))
            Text(
                text = stringResource(R.string.settings_privacy_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            SettingsRow(
                icon = { Icon(Icons.Filled.Block, contentDescription = null, tint = MaterialTheme.swiply.textSecondary) },
                text = stringResource(R.string.settings_blocked),
                onClick = onOpenBlocked,
            )
            SettingsRow(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.swiply.textSecondary,
                    )
                },
                text = stringResource(R.string.settings_logout),
                onClick = viewModel::logout,
            )
            SettingsRow(
                icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.swiply.danger) },
                text = stringResource(R.string.settings_delete),
                textColor = MaterialTheme.swiply.danger,
                onClick = { showDeleteDialog = true },
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    text: String,
    textColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor ?: MaterialTheme.swiply.textPrimary,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}
