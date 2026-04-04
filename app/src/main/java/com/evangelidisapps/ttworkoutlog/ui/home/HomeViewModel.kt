package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val countByDate: Map<LocalDate, Int> = emptyMap(),
    val thisWeekCount: Int = 0
)

data class WorkoutFilter(
    val query: String = "",
    val style: String? = null,     // null = all styles
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isActive: Boolean
        get() = query.isNotBlank() || style != null || startDate != null || endDate != null
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val repository = WorkoutRepository.create(application)
    private val prefsRepo = UserPreferencesRepository(application)

    private val _filter = MutableStateFlow(WorkoutFilter())
    val filter = _filter.asStateFlow()

    val workouts = repository
        .observeWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val filteredWorkouts = combine(
        repository.observeWorkouts(),
        _filter
    ) { list, f ->
        if (!f.isActive) list
        else list.filter { workout ->
            val matchesQuery = f.query.isBlank() ||
                workout.title.contains(f.query, ignoreCase = true)
            val matchesStyle = f.style == null || workout.style == f.style
            val workoutDate = runCatching { LocalDate.parse(workout.dateLabel) }.getOrNull()
            val matchesStart = f.startDate == null ||
                (workoutDate != null && !workoutDate.isBefore(f.startDate))
            val matchesEnd = f.endDate == null ||
                (workoutDate != null && !workoutDate.isAfter(f.endDate))
            matchesQuery && matchesStyle && matchesStart && matchesEnd
        }
    }.stateIn(
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

    val streakData = repository.observeWorkouts()
        .map { computeStreakData(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreakData()
        )

    // ── Filter actions ────────────────────────────────────────────────────────

    fun setSearchQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setStyleFilter(style: String?) = _filter.update { it.copy(style = style) }
    fun setStartDate(date: LocalDate?) = _filter.update { it.copy(startDate = date) }
    fun setEndDate(date: LocalDate?) = _filter.update { it.copy(endDate = date) }
    fun clearFilters() { _filter.value = WorkoutFilter() }

    // ── Workout actions ───────────────────────────────────────────────────────

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

    fun saveAsTemplate(workoutId: String) {
        viewModelScope.launch {
            repository.saveAsTemplate(workoutId)
        }
    }

    private fun computeStreakData(workouts: List<Workout>): StreakData {
        val countByDate = workouts
            .mapNotNull { runCatching { LocalDate.parse(it.dateLabel) }.getOrNull() }
            .groupingBy { it }
            .eachCount()

        // Current streak: count back from today; if today is empty try from yesterday
        fun streakBackFrom(start: LocalDate): Int {
            var n = 0; var d = start
            while (countByDate.containsKey(d)) { n++; d = d.minusDays(1) }
            return n
        }
        val today = LocalDate.now()
        val current = streakBackFrom(today).let { if (it > 0) it else streakBackFrom(today.minusDays(1)) }

        // Longest streak across history
        var longest = 0; var run = 0; var prev: LocalDate? = null
        for (d in countByDate.keys.sorted()) {
            run = if (prev != null && d == prev!!.plusDays(1)) run + 1 else 1
            if (run > longest) longest = run
            prev = d
        }

        // This week (Mon–today)
        val weekStart = today.with(DayOfWeek.MONDAY)
        val thisWeek = countByDate.entries.count { it.key in weekStart..today }

        return StreakData(current, longest, countByDate, thisWeek)
    }
}
