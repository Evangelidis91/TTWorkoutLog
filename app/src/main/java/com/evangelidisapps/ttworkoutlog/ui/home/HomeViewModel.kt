package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val repository = WorkoutRepository.create(application)
    private val prefsRepo = UserPreferencesRepository(application)

    val workouts = repository
        .observeWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val workoutsWithDetails = repository
        .observeWorkoutsWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<WorkoutWithDetails>()
        )

    val dateFormat = prefsRepo.dateFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "dd/MM/yyyy"
        )

    val weightUnit = prefsRepo.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "kg"
        )

    val distanceUnit = prefsRepo.distanceUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "km"
        )

    fun addQuickWorkout(note: String? = null) {
        val today = LocalDate.now()
        viewModelScope.launch {
            repository.upsert(
                Workout(
                    id = UUID.randomUUID().toString(),
                    title = "Workout",
                    dateLabel = today.format(dateFormatter),
                    note = note,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.delete(workout)
        }
    }

    fun undoDelete(workout: Workout) {
        viewModelScope.launch {
            repository.upsert(workout)
        }
    }
}
