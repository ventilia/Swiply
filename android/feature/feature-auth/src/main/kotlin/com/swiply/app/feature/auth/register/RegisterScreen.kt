package com.swiply.app.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.model.Gender
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.PillShape
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.auth.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (state.birthDate ?: LocalDate.of(2000, 1, 1))
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.onBirthDateChanged(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.auth_back))
                }
            },
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.auth_register_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.auth_register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.swiply.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        SwiplyTextField(
            value = state.displayName,
            onValueChange = viewModel::onNameChanged,
            label = stringResource(R.string.auth_name),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_name"),
        )
        Spacer(Modifier.height(12.dp))
        SwiplyTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChanged,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email"),
        )
        Spacer(Modifier.height(12.dp))
        SwiplyTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChanged,
            label = stringResource(R.string.auth_password),
            isPassword = true,
            supportingText = stringResource(R.string.auth_password_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password"),
        )

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showDatePicker = true },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_birthdate"),
        ) {
            Text(
                text = state.birthDate
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                    ?: stringResource(R.string.auth_birth_date_pick),
                color = if (state.underage) MaterialTheme.swiply.danger else MaterialTheme.swiply.textPrimary,
            )
        }
        if (state.underage) {
            Text(
                text = stringResource(R.string.auth_underage_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.danger,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Text(
            text = stringResource(R.string.auth_gender),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        GenderChips(
            selected = state.gender?.let { setOf(it) } ?: emptySet(),
            labels = mapOf(
                Gender.MALE to stringResource(R.string.auth_gender_male),
                Gender.FEMALE to stringResource(R.string.auth_gender_female),
                Gender.OTHER to stringResource(R.string.auth_gender_other),
            ),
            onToggle = viewModel::onGenderChanged,
        )

        Text(
            text = stringResource(R.string.auth_interested_in),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        GenderChips(
            selected = state.interestedIn,
            labels = mapOf(
                Gender.MALE to stringResource(R.string.auth_show_male),
                Gender.FEMALE to stringResource(R.string.auth_show_female),
                Gender.OTHER to stringResource(R.string.auth_show_other),
            ),
            onToggle = viewModel::onInterestToggled,
        )


        Text(
            text = stringResource(R.string.auth_interests),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        com.swiply.app.core.ui.components.InterestChipsSelector(
            all = com.swiply.app.core.model.Interests.CATALOG,
            selected = state.interests,
            onToggle = viewModel::onHobbyToggled,
            modifier = Modifier.fillMaxWidth(),
        )


        val language by viewModel.language.collectAsStateWithLifecycle()
        Text(
            text = stringResource(R.string.auth_language),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            LangChip("RU", language == com.swiply.app.core.datastore.AppLanguage.RUSSIAN) {
                viewModel.setLanguage(com.swiply.app.core.datastore.AppLanguage.RUSSIAN)
            }
            LangChip("EN", language == com.swiply.app.core.datastore.AppLanguage.ENGLISH) {
                viewModel.setLanguage(com.swiply.app.core.datastore.AppLanguage.ENGLISH)
            }
            LangChip(stringResource(R.string.auth_lang_system), language == com.swiply.app.core.datastore.AppLanguage.SYSTEM) {
                viewModel.setLanguage(com.swiply.app.core.datastore.AppLanguage.SYSTEM)
            }
        }

        if (state.error != null) {
            Text(
                text = state.error.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.danger,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = stringResource(R.string.auth_register_button),
            onClick = viewModel::submit,
            enabled = state.canSubmit,
            loading = state.isLoading,
            modifier = Modifier.testTag("register_submit"),
        )
        Text(
            text = stringResource(R.string.auth_terms_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.swiply.textSecondary,
            modifier = Modifier.padding(top = 10.dp),
        )

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.textSecondary,
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(R.string.auth_login_link),
                    color = MaterialTheme.swiply.gradientStart,
                )
            }
        }
    }
}

@Composable
private fun GenderChips(
    selected: Set<Gender>,
    labels: Map<Gender, String>,
    onToggle: (Gender) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { (gender, label) ->
            FilterChip(
                selected = gender in selected,
                onClick = { onToggle(gender) },
                label = { Text(label) },
                shape = PillShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.swiply.brandContainer,
                    selectedLabelColor = MaterialTheme.swiply.brand,
                ),
            )
        }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = PillShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.swiply.brandContainer,
            selectedLabelColor = MaterialTheme.swiply.brand,
        ),
    )
}
