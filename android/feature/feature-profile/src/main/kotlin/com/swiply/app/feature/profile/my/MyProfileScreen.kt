package com.swiply.app.feature.profile.my

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiply.app.core.ui.components.FullScreenLoading
import com.swiply.app.core.ui.components.PhotoPager
import com.swiply.app.core.ui.theme.CardShape
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.profile.R

@Composable
fun MyProfileScreen(
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onVerifyEmail: () -> Unit,
    viewModel: MyProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val current = profile

    if (current == null) {
        FullScreenLoading()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Row {
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.profile_edit))
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.profile_settings))
                }
            }
        }

        if (!current.emailVerified) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.swiply.brandContainer)
                    .clickable(onClick = onVerifyEmail)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_email_unverified),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.swiply.brand,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.profile_email_verify),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.swiply.brand,
                )
            }
        }

        PhotoPager(
            photoUrls = current.photos.mapNotNull { it.url },
            contentDescription = current.displayName,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(horizontal = 16.dp)
                .clip(CardShape),
        )

        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${current.displayName}, ${current.age}",
                style = MaterialTheme.typography.headlineMedium,
            )
            if (current.isVerified) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = stringResource(R.string.profile_verified),
                    tint = MaterialTheme.swiply.superlike,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        current.city?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.swiply.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Text(
            text = current.bio ?: stringResource(R.string.profile_no_bio),
            style = MaterialTheme.typography.bodyLarge,
            color = if (current.bio != null) {
                MaterialTheme.swiply.textPrimary
            } else {
                MaterialTheme.swiply.textSecondary
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        if (current.interests.isNotEmpty()) {
            com.swiply.app.core.ui.components.InterestChipsRow(
                interests = current.interests,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        val percent = (current.completeness * 100).toInt()
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.profile_completeness, percent),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.swiply.textSecondary,
            )
            LinearProgressIndicator(
                progress = { current.completeness },
                color = MaterialTheme.swiply.gradientStart,
                trackColor = MaterialTheme.swiply.surfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
        }

        OutlinedButton(
            onClick = onEditProfile,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(stringResource(R.string.profile_edit))
        }
        Spacer(Modifier.height(80.dp))
    }
}
