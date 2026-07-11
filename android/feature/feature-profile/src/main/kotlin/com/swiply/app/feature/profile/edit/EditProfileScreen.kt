package com.swiply.app.feature.profile.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.Photo
import com.swiply.app.core.model.PhotoStatus
import com.swiply.app.core.ui.components.FullScreenLoading
import com.swiply.app.core.ui.components.GradientButton
import com.swiply.app.core.ui.components.SwiplyTextField
import com.swiply.app.core.ui.theme.PillShape
import com.swiply.app.core.ui.theme.swiply
import com.swiply.app.feature.profile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(viewModel::uploadPhoto)
    }

    if (state.profile == null && state.error == null) {
        FullScreenLoading()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_title)) },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // ===== Фото =====
            Text(
                text = stringResource(R.string.edit_photos),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.edit_photos_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.textSecondary,
                modifier = Modifier.padding(bottom = 10.dp),
            )

            PhotoGrid(
                photos = state.profile?.photos.orEmpty(),
                isUploading = state.isUploadingPhoto,
                onAdd = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onDelete = viewModel::deletePhoto,
                onMove = viewModel::movePhoto,
            )

            // ===== Поля =====
            Spacer(Modifier.height(20.dp))
            SwiplyTextField(
                value = state.displayName,
                onValueChange = viewModel::onNameChanged,
                label = stringResource(R.string.edit_name),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            SwiplyTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChanged,
                label = stringResource(R.string.edit_bio),
                singleLine = false,
                minLines = 3,
                supportingText = "${state.bio.length}/600",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            SwiplyTextField(
                value = state.city,
                onValueChange = viewModel::onCityChanged,
                label = stringResource(R.string.edit_city),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.edit_gender),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
            )
            GenderChipRow(
                selected = state.gender?.let { setOf(it) } ?: emptySet(),
                onToggle = viewModel::onGenderChanged,
            )

            Text(
                text = stringResource(R.string.edit_interested_in),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
            )
            GenderChipRow(
                selected = state.interestedIn,
                onToggle = viewModel::onInterestToggled,
            )

            // ===== Интересы / хобби =====
            Text(
                text = stringResource(R.string.edit_interests),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
            )
            com.swiply.app.core.ui.components.InterestChipsSelector(
                all = com.swiply.app.core.model.Interests.CATALOG,
                selected = state.interests,
                onToggle = viewModel::onHobbyToggled,
                modifier = Modifier.fillMaxWidth(),
            )

            // ===== Параметры поиска =====
            Text(
                text = stringResource(R.string.edit_search_prefs),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 26.dp, bottom = 6.dp),
            )

            Text(
                text = stringResource(R.string.edit_age_range, state.minAge, state.maxAge),
                style = MaterialTheme.typography.bodyMedium,
            )
            var ageRange by remember(state.profile) {
                mutableStateOf(state.minAge.toFloat()..state.maxAge.toFloat())
            }
            RangeSlider(
                value = ageRange,
                onValueChange = { ageRange = it },
                onValueChangeFinished = {
                    viewModel.onAgeRangeChanged(
                        ageRange.start.toInt(),
                        ageRange.endInclusive.toInt(),
                    )
                },
                valueRange = 18f..100f,
            )

            Text(
                text = stringResource(R.string.edit_distance, state.maxDistanceKm),
                style = MaterialTheme.typography.bodyMedium,
            )
            var distance by remember(state.profile) { mutableStateOf(state.maxDistanceKm.toFloat()) }
            Slider(
                value = distance,
                onValueChange = { distance = it },
                onValueChangeFinished = { viewModel.onDistanceChanged(distance.toInt()) },
                valueRange = 1f..300f,
            )

            ToggleRow(
                title = stringResource(R.string.edit_incognito),
                subtitle = stringResource(R.string.edit_incognito_hint),
                checked = state.isIncognito,
                onCheckedChange = viewModel::onIncognitoChanged,
            )
            ToggleRow(
                title = stringResource(R.string.edit_discoverable),
                subtitle = stringResource(R.string.edit_discoverable_hint),
                checked = state.isDiscoverable,
                onCheckedChange = viewModel::onDiscoverableChanged,
            )

            if (state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.swiply.danger,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            GradientButton(
                text = if (state.savedFlash) {
                    stringResource(R.string.edit_saved)
                } else {
                    stringResource(R.string.edit_save)
                },
                onClick = viewModel::save,
                enabled = state.canSave,
                loading = state.isSaving,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: List<Photo>,
    isUploading: Boolean,
    onAdd: () -> Unit,
    onDelete: (java.util.UUID) -> Unit,
    onMove: (java.util.UUID, Int) -> Unit,
) {
    val cells = photos.size + if (photos.size < 6) 1 else 0
    val rows = (cells + 2) / 3
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height((rows * 150).dp),
    ) {
        items(photos, key = { it.id }) { photo ->
            Box(
                modifier = Modifier
                    .aspectRatio(0.78f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.swiply.surfaceElevated),
            ) {
                AsyncImage(
                    model = photo.thumbUrl ?: photo.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (photo.status != PhotoStatus.APPROVED) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(
                                if (photo.status == PhotoStatus.PENDING) {
                                    R.string.edit_photo_pending
                                } else {
                                    R.string.edit_photo_rejected
                                },
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                }
                IconButton(
                    onClick = { onDelete(photo.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.edit_delete_photo),
                        tint = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), PillShape)
                            .padding(4.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp),
                ) {
                    IconButton(onClick = { onMove(photo.id, -1) }, modifier = Modifier.height(28.dp)) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = stringResource(R.string.edit_move_left),
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = { onMove(photo.id, +1) }, modifier = Modifier.height(28.dp)) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.edit_move_right),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
        if (photos.size < 6) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(0.78f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.swiply.surfaceElevated)
                        .let { it },
                ) {
                    IconButton(onClick = onAdd, enabled = !isUploading) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.height(24.dp))
                        } else {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(R.string.edit_add_photo),
                                tint = MaterialTheme.swiply.gradientStart,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderChipRow(
    selected: Set<Gender>,
    onToggle: (Gender) -> Unit,
) {
    val labels = mapOf(
        Gender.MALE to stringResource(R.string.edit_gender_male),
        Gender.FEMALE to stringResource(R.string.edit_gender_female),
        Gender.OTHER to stringResource(R.string.edit_gender_other),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { (gender, label) ->
            FilterChip(
                selected = gender in selected,
                onClick = { onToggle(gender) },
                label = { Text(label) },
                shape = PillShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.swiply.gradientEnd.copy(alpha = 0.18f),
                    selectedLabelColor = MaterialTheme.swiply.gradientEnd,
                ),
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.swiply.textSecondary,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
