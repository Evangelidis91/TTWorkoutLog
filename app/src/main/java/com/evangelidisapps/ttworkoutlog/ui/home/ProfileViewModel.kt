package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isGuest: Boolean = false,
    val totalWorkouts: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val weightUnit: String = "kg"
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository.create(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.observeTotalWorkouts(),
        repository.observeTotalVolumeKg(),
        prefsRepo.weightUnit,
        prefsRepo.isGuest
    ) { totalWorkouts, totalVolumeKg, weightUnit, isGuest ->
        ProfileUiState(
            displayName = when {
                isGuest -> "Guest"
                firebaseUser?.displayName?.isNotBlank() == true -> firebaseUser.displayName!!
                firebaseUser?.email != null -> firebaseUser.email!!
                else -> "Athlete"
            },
            email = firebaseUser?.email.orEmpty(),
            photoUrl = firebaseUser?.photoUrl?.toString(),
            isGuest = isGuest,
            totalWorkouts = totalWorkouts,
            totalVolumeKg = totalVolumeKg,
            weightUnit = weightUnit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )
}
