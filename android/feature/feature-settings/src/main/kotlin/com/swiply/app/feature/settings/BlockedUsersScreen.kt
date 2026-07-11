package com.swiply.app.feature.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.ui.components.Avatar
import com.swiply.app.core.ui.components.EmptyState
import com.swiply.app.core.ui.theme.swiply

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadBlocked() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.blocked_title)) },
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
        if (actions.blocked.isEmpty()) {
            EmptyState(
                illustration = com.swiply.app.core.ui.components.SwiplyIllustration.Blocked,
                title = stringResource(R.string.blocked_empty),
                subtitle = null,
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                items(actions.blocked, key = { it.userId }) { blocked ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Avatar(url = null, name = blocked.displayName ?: "?", size = 44.dp)
                        Text(
                            text = blocked.displayName ?: blocked.userId.toString().take(8),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        )
                        TextButton(onClick = { viewModel.unblock(blocked.userId) }) {
                            Text(
                                text = stringResource(R.string.blocked_unblock),
                                color = MaterialTheme.swiply.gradientStart,
                            )
                        }
                    }
                }
            }
        }
    }
}
