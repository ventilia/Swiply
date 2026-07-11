package com.swiply.app.feature.profile.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.model.MyProfile
import com.swiply.app.feature.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    /** Room-кэш даёт мгновенный рендер; сеть обновляет при заходе на экран */
    val profile: StateFlow<MyProfile?> = profileRepository.myProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { profileRepository.refreshMyProfile() }
    }
}
