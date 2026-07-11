package com.swiply.app.feature.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.MyProfile
import com.swiply.app.feature.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditProfileUiState(
    val profile: MyProfile? = null,
    val displayName: String = "",
    val bio: String = "",
    val city: String = "",
    val gender: Gender? = null,
    val interestedIn: Set<Gender> = emptySet(),
    val interests: Set<String> = emptySet(),
    val minAge: Int = 18,
    val maxAge: Int = 100,
    val maxDistanceKm: Int = 50,
    val isIncognito: Boolean = false,
    val isDiscoverable: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val savedFlash: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean
        get() = profile != null && displayName.trim().length >= 2 && interestedIn.isNotEmpty() && !isSaving
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state

    init {
        viewModelScope.launch {
            when (val result = profileRepository.refreshMyProfile()) {
                is AppResult.Success -> applyProfile(result.data)
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    private fun applyProfile(profile: MyProfile) {
        _state.update {
            it.copy(
                profile = profile,
                displayName = profile.displayName,
                bio = profile.bio.orEmpty(),
                city = profile.city.orEmpty(),
                gender = profile.gender,
                interestedIn = profile.interestedIn,
                interests = profile.interests.toSet(),
                minAge = profile.minAgePref,
                maxAge = profile.maxAgePref,
                maxDistanceKm = profile.maxDistanceKm,
                isIncognito = profile.isIncognito,
                isDiscoverable = profile.isDiscoverable,
                isUploadingPhoto = false,
                isSaving = false,
            )
        }
    }

    fun onNameChanged(value: String) = _state.update { it.copy(displayName = value, savedFlash = false) }
    fun onBioChanged(value: String) = _state.update { it.copy(bio = value.take(600), savedFlash = false) }
    fun onCityChanged(value: String) = _state.update { it.copy(city = value, savedFlash = false) }
    fun onGenderChanged(value: Gender) = _state.update { it.copy(gender = value, savedFlash = false) }

    fun onInterestToggled(value: Gender) = _state.update {
        val updated = if (value in it.interestedIn) it.interestedIn - value else it.interestedIn + value
        it.copy(interestedIn = updated, savedFlash = false)
    }

    /** Хобби/интересы: не больше [Interests.MAX_SELECTED] */
    fun onHobbyToggled(value: String) = _state.update {
        val updated = when {
            value in it.interests -> it.interests - value
            it.interests.size >= com.swiply.app.core.model.Interests.MAX_SELECTED -> it.interests
            else -> it.interests + value
        }
        it.copy(interests = updated, savedFlash = false)
    }

    fun onAgeRangeChanged(min: Int, max: Int) = _state.update {
        it.copy(minAge = min.coerceIn(18, 100), maxAge = max.coerceIn(18, 100), savedFlash = false)
    }

    fun onDistanceChanged(km: Int) = _state.update { it.copy(maxDistanceKm = km.coerceIn(1, 300), savedFlash = false) }
    fun onIncognitoChanged(value: Boolean) = _state.update { it.copy(isIncognito = value, savedFlash = false) }
    fun onDiscoverableChanged(value: Boolean) = _state.update { it.copy(isDiscoverable = value, savedFlash = false) }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val profileResult = profileRepository.updateProfile(
                displayName = s.displayName,
                bio = s.bio,
                gender = s.gender,
                interestedIn = s.interestedIn,
                interests = s.interests.toList(),
                city = s.city,
            )
            if (profileResult is AppResult.Failure) {
                _state.update { it.copy(isSaving = false, error = profileResult.error.message) }
                return@launch
            }
            val prefsResult = profileRepository.updatePreferences(
                minAge = s.minAge,
                maxAge = s.maxAge,
                maxDistanceKm = s.maxDistanceKm,
                isIncognito = s.isIncognito,
                isDiscoverable = s.isDiscoverable,
            )
            when (prefsResult) {
                is AppResult.Success -> {
                    applyProfile(prefsResult.data)
                    _state.update { it.copy(savedFlash = true) }
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, error = prefsResult.error.message)
                }
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        _state.update { it.copy(isUploadingPhoto = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.uploadPhoto(uri)) {
                is AppResult.Success -> applyProfile(result.data)
                is AppResult.Failure -> _state.update {
                    it.copy(isUploadingPhoto = false, error = result.error.message)
                }
            }
        }
    }

    fun deletePhoto(photoId: UUID) {
        viewModelScope.launch {
            when (val result = profileRepository.deletePhoto(photoId)) {
                is AppResult.Success -> applyProfile(result.data)
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    /** Перестановка фото: двигаем на позицию влево/вправо */
    fun movePhoto(photoId: UUID, delta: Int) {
        val photos = _state.value.profile?.photos ?: return
        val index = photos.indexOfFirst { it.id == photoId }
        val target = index + delta
        if (index < 0 || target < 0 || target >= photos.size) return
        val reordered = photos.toMutableList().apply {
            val item = removeAt(index)
            add(target, item)
        }
        viewModelScope.launch {
            when (val result = profileRepository.reorderPhotos(reordered.map { it.id })) {
                is AppResult.Success -> applyProfile(result.data)
                is AppResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }
}
