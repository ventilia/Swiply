package com.swiply.app.feature.profile.publicprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.swiply.app.core.model.ReportReason
import com.swiply.app.core.ui.components.ErrorState
import com.swiply.app.core.ui.components.FullScreenLoading
import com.swiply.app.core.ui.components.PhotoPager
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.profile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.blocked) {
        if (state.blocked) onBack()
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(stringResource(R.string.public_block)) },
            text = { Text(stringResource(R.string.public_block_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockDialog = false
                        viewModel.block()
                    },
                ) { Text(stringResource(R.string.common_confirm), color = MaterialTheme.swiply.danger) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, description ->
                showReportDialog = false
                viewModel.report(reason, description)
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = stringResource(R.string.public_report),
                            tint = MaterialTheme.swiply.textSecondary,
                        )
                    }
                    IconButton(onClick = { showBlockDialog = true }) {
                        Icon(
                            Icons.Filled.Block,
                            contentDescription = stringResource(R.string.public_block),
                            tint = MaterialTheme.swiply.danger,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val profile = state.profile
        when {
            state.isLoading -> FullScreenLoading(Modifier.padding(padding))
            profile == null -> ErrorState(
                message = state.error ?: "",
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                PhotoPager(
                    photoUrls = profile.photos.map { it.url },
                    contentDescription = profile.displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp),
                )

                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${profile.displayName}, ${profile.age}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    if (profile.isVerified) {
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.swiply.superlike,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (profile.isOnline) {
                                    MaterialTheme.swiply.online
                                } else {
                                    MaterialTheme.swiply.outline
                                },
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = stringResource(
                            if (profile.isOnline) R.string.public_online else R.string.public_offline,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.swiply.textSecondary,
                    )
                    profile.distanceKm?.let {
                        Text(
                            text = stringResource(R.string.public_distance, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.swiply.textSecondary,
                        )
                    }
                    profile.city?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.swiply.textSecondary,
                        )
                    }
                }

                profile.bio?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
                if (profile.interests.isNotEmpty()) {
                    com.swiply.app.core.ui.components.InterestChipsRow(
                        interests = profile.interests,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }

                if (state.reportSent) {
                    Text(
                        text = stringResource(R.string.public_report_sent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.swiply.success,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (ReportReason, String?) -> Unit,
) {
    var selectedReason by remember { mutableStateOf(ReportReason.SPAM) }
    var description by remember { mutableStateOf("") }

    val labels = mapOf(
        ReportReason.SPAM to stringResource(R.string.report_spam),
        ReportReason.FAKE_PROFILE to stringResource(R.string.report_fake),
        ReportReason.INAPPROPRIATE_CONTENT to stringResource(R.string.report_inappropriate),
        ReportReason.HARASSMENT to stringResource(R.string.report_harassment),
        ReportReason.UNDERAGE to stringResource(R.string.report_underage),
        ReportReason.OTHER to stringResource(R.string.report_other),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.public_report_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                labels.forEach { (reason, label) ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(label) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                SwiplyTextField(
                    value = description,
                    onValueChange = { description = it.take(1000) },
                    label = stringResource(R.string.public_report_hint),
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selectedReason, description.ifBlank { null }) }) {
                Text(stringResource(R.string.common_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}
