package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.evangelidisapps.ttworkoutlog.data.model.CatalogExercise
import com.evangelidisapps.ttworkoutlog.data.model.Exercise
import com.evangelidisapps.ttworkoutlog.data.model.Workout
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutExercise
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutSet
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails
import com.evangelidisapps.ttworkoutlog.data.repository.ExerciseCatalogRepository
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

data class EditableSetState(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int = 1,
    val reps: String = "",
    val weight: String = "",
    val durationSec: String = "",
    val distanceMeters: String = "",
    val calories: String = "",
    val isWarmup: Boolean = false,
    val note: String = "",
    val setType: String = "strength"
)

data class EditableExerciseState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val muscleGroup: String = "",
    val note: String = "",
    val isSuperset: Boolean = false,
    val sets: List<EditableSetState> = listOf(EditableSetState())
)

data class CatalogFilter(
    val query: String = "",
    val equipment: String? = null,
    val muscle: String? = null,
    val category: String? = null
)

data class WorkoutEditState(
    val workoutId: String = UUID.randomUUID().toString(),
    val title: String = "",
    val dateLabel: String = "",
    val note: String = "",
    val style: String = "Standard",
    val durationSec: String = "",
    val calories: String = "",
    val createdAt: Long = 0L, // 0 = new workout, will be set on first save
    val exercises: List<EditableExerciseState> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val showExercisePicker: Boolean = false
)

class WorkoutEditViewModel(
    private val repository: WorkoutRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val catalogRepository: ExerciseCatalogRepository,
    private val existingWorkoutId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutEditState())
    val state = _state.asStateFlow()

    val weightUnit = prefsRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "kg")

    val distanceUnit = prefsRepository.distanceUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "km")

    // ── Exercise catalog picker ───────────────────────────────────────────────

    private val _catalogFilter = MutableStateFlow(CatalogFilter())
    val catalogFilter = _catalogFilter.asStateFlow()

    private val _selectedExerciseDetail = MutableStateFlow<CatalogExercise?>(null)
    val selectedExerciseDetail = _selectedExerciseDetail.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val catalogResults = _catalogFilter
        .flatMapLatest { f ->
            catalogRepository.search(f.query, f.equipment, f.muscle, f.category)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openExercisePicker() = _state.update { it.copy(showExercisePicker = true) }
    fun closeExercisePicker() {
        _state.update { it.copy(showExercisePicker = false) }
        _selectedExerciseDetail.value = null
        _catalogFilter.value = CatalogFilter()
    }

    fun openExerciseDetail(exercise: CatalogExercise) { _selectedExerciseDetail.value = exercise }
    fun closeExerciseDetail() { _selectedExerciseDetail.value = null }

    fun setCatalogQuery(query: String) = _catalogFilter.update { it.copy(query = query) }
    fun setCatalogEquipment(equipment: String?) = _catalogFilter.update { it.copy(equipment = equipment) }
    fun setCatalogMuscle(muscle: String?) = _catalogFilter.update { it.copy(muscle = muscle) }
    fun setCatalogCategory(category: String?) = _catalogFilter.update { it.copy(category = category) }
    fun clearCatalogFilters() { _catalogFilter.value = CatalogFilter() }

    fun addExerciseFromCatalog(exercise: CatalogExercise) {
        _state.update { current ->
            current.copy(
                exercises = current.exercises + EditableExerciseState(
                    id = UUID.randomUUID().toString(),
                    name = exercise.name,
                    muscleGroup = exercise.primaryMuscles.firstOrNull().orEmpty(),
                    sets = listOf(EditableSetState(setNumber = 1))
                ),
                showExercisePicker = false
            )
        }
        _selectedExerciseDetail.value = null
        _catalogFilter.value = CatalogFilter()
    }

    init {
        if (!existingWorkoutId.isNullOrBlank()) {
            viewModelScope.launch {
                val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
                val unit = prefsRepository.weightUnit.firstOrNull() ?: "kg"
                val distUnit = prefsRepository.distanceUnit.firstOrNull() ?: "km"
                repository.observeWorkoutWithDetails(existingWorkoutId).firstOrNull()?.let { details ->
                    _state.update { current ->
                        current.copy(
                            workoutId = details.workout.id,
                            title = details.workout.title,
                            dateLabel = isoToDisplay(details.workout.dateLabel, fmt),
                            note = details.workout.note.orEmpty(),
                            style = details.workout.style ?: "Standard",
                            durationSec = details.workout.durationSec?.toString().orEmpty(),
                            calories = details.workout.calories?.toString().orEmpty(),
                            createdAt = details.workout.createdAt,
                            exercises = details.exercises.map { ex ->
                                EditableExerciseState(
                                    id = ex.id,
                                    name = ex.exercise.name,
                                    muscleGroup = ex.exercise.muscleGroup.orEmpty(),
                                    note = ex.note.orEmpty(),
                                    isSuperset = ex.isSuperset,
                                    sets = ex.sets.map { set ->
                                        EditableSetState(
                                            id = set.id,
                                            setNumber = set.setNumber,
                                            reps = set.reps?.toString().orEmpty(),
                                            weight = set.weight?.let { kgToDisplayUnit(it, unit) }.orEmpty(),
                                            durationSec = set.durationSec?.toString().orEmpty(),
                                            distanceMeters = set.distanceMeters?.let { metersToDisplayUnit(it, distUnit) }.orEmpty(),
                                            calories = set.calories?.toString().orEmpty(),
                                            isWarmup = set.isWarmup,
                                            note = set.note.orEmpty(),
                                            setType = set.setType
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val defaultStyle = prefsRepository.defaultStyle.firstOrNull() ?: "Standard"
                val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
                _state.update {
                    it.copy(
                        style = defaultStyle,
                        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern(fmt))
                    )
                }
            }
        }
    }

    fun updateTitle(value: String) = _state.update { it.copy(title = value) }
    fun updateNote(value: String) = _state.update { it.copy(note = value) }
    fun updateStyle(value: String) = _state.update { it.copy(style = value) }
    fun updateDuration(value: String) = _state.update { it.copy(durationSec = value) }
    fun updateCalories(value: String) = _state.update { it.copy(calories = value) }
    fun updateDateLabel(value: String) = _state.update { it.copy(dateLabel = value) }

    fun addExercise() {
        _state.update { current ->
            current.copy(
                exercises = current.exercises + newExercise(current.exercises.size)
            )
        }
    }

    private fun newExercise(position: Int): EditableExerciseState =
        EditableExerciseState(
            id = UUID.randomUUID().toString(),
            name = "",
            sets = listOf(EditableSetState(setNumber = 1))
        )

    fun removeExercise(exerciseId: String) {
        _state.update { current ->
            current.copy(exercises = current.exercises.filterNot { it.id == exerciseId })
        }
    }

    fun updateExerciseName(id: String, name: String) {
        _state.updateListExercise(id) { it.copy(name = name) }
    }

    fun updateExerciseNote(id: String, note: String) {
        _state.updateListExercise(id) { it.copy(note = note) }
    }

    fun updateExerciseMuscle(id: String, muscle: String) {
        _state.updateListExercise(id) { it.copy(muscleGroup = muscle) }
    }

    fun toggleSuperset(id: String, value: Boolean) {
        _state.updateListExercise(id) { it.copy(isSuperset = value) }
    }

    fun addSet(exerciseId: String) {
        _state.updateListExercise(exerciseId) { ex ->
            ex.copy(
                sets = ex.sets + EditableSetState(
                    setNumber = ex.sets.size + 1
                )
            )
        }
    }

    fun removeSet(exerciseId: String, setId: String) {
        _state.updateListExercise(exerciseId) { ex ->
            ex.copy(sets = ex.sets.filterNot { it.id == setId })
        }
    }

    fun updateSet(
        exerciseId: String,
        setId: String,
        reps: String? = null,
        weight: String? = null,
        durationSec: String? = null,
        distanceMeters: String? = null,
        calories: String? = null,
        note: String? = null,
        setType: String? = null,
        isWarmup: Boolean? = null
    ) {
        _state.updateListExercise(exerciseId) { ex ->
            ex.copy(
                sets = ex.sets.map { set ->
                    if (set.id != setId) return@map set
                    set.copy(
                        reps = reps ?: set.reps,
                        weight = weight ?: set.weight,
                        durationSec = durationSec ?: set.durationSec,
                        distanceMeters = distanceMeters ?: set.distanceMeters,
                        calories = calories ?: set.calories,
                        note = note ?: set.note,
                        setType = setType ?: set.setType,
                        isWarmup = isWarmup ?: set.isWarmup
                    )
                }
            )
        }
    }

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
            val unit = prefsRepository.weightUnit.firstOrNull() ?: "kg"
            val distUnit = prefsRepository.distanceUnit.firstOrNull() ?: "km"
            runCatching {
                repository.upsertWithDetails(
                    WorkoutWithDetails(
                        workout = Workout(
                            id = current.workoutId,
                            title = current.title.ifBlank { "Workout" },
                            dateLabel = displayToIso(current.dateLabel, fmt)
                                ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            note = current.note.ifBlank { null },
                            durationSec = current.durationSec.toIntOrNull(),
                            calories = current.calories.toIntOrNull(),
                            style = current.style.ifBlank { "Standard" },
                            createdAt = if (current.createdAt == 0L) System.currentTimeMillis()
                                        else current.createdAt
                        ),
                        exercises = current.exercises.mapIndexed { index, ex ->
                            val exerciseId = ex.id.ifBlank { UUID.randomUUID().toString() }
                            val exercise = Exercise(
                                id = exerciseId,
                                name = ex.name.ifBlank { "Exercise ${index + 1}" },
                                muscleGroup = ex.muscleGroup.ifBlank { null },
                                notes = ex.note.ifBlank { null }
                            )
                            WorkoutExercise(
                                id = exerciseId,
                                workoutId = current.workoutId,
                                exercise = exercise,
                                position = index,
                                note = ex.note.ifBlank { null },
                                isSuperset = ex.isSuperset,
                                sets = ex.sets.mapIndexed { setIndex, set ->
                                    WorkoutSet(
                                        id = set.id.ifBlank { UUID.randomUUID().toString() },
                                        workoutExerciseId = exerciseId,
                                        setNumber = setIndex + 1,
                                        reps = set.reps.toIntOrNull(),
                                        weight = set.weight.toDoubleOrNull()?.let { displayUnitToKg(it, unit) },
                                        durationSec = set.durationSec.toIntOrNull(),
                                        distanceMeters = set.distanceMeters.toDoubleOrNull()?.let { displayUnitToMeters(it, distUnit) },
                                        calories = set.calories.toIntOrNull(),
                                        isWarmup = set.isWarmup,
                                        note = set.note.ifBlank { null },
                                        setType = set.setType.ifBlank { "strength" }
                                    )
                                }
                            )
                        }
                    )
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.localizedMessage ?: "Failed to save workout"
                    )
                }
            }
        }
    }

    private fun MutableStateFlow<WorkoutEditState>.updateListExercise(
        id: String,
        transform: (EditableExerciseState) -> EditableExerciseState
    ) {
        update { current ->
            current.copy(
                exercises = current.exercises.map { ex ->
                    if (ex.id == id) transform(ex) else ex
                }
            )
        }
    }

    companion object {
        fun kgToDisplayUnit(kg: Double, unit: String): String {
            val value = if (unit == "lbs") kg * 2.20462 else kg
            return if (value == value.toLong().toDouble()) value.toLong().toString()
                   else "%.1f".format(value)
        }

        fun displayUnitToKg(value: Double, unit: String): Double =
            if (unit == "lbs") value * 0.453592 else value

        fun metersToDisplayUnit(meters: Int, unit: String): String {
            val value = if (unit == "miles") meters / 1609.344 else meters / 1000.0
            return "%.2f".format(value)
        }

        fun displayUnitToMeters(value: Double, unit: String): Int =
            if (unit == "miles") (value * 1609.344).toInt() else (value * 1000).toInt()

        fun isoToDisplay(isoDate: String, pattern: String): String =
            runCatching {
                LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern(pattern))
            }.getOrElse { isoDate }

        fun displayToIso(displayDate: String, pattern: String): String? =
            runCatching {
                LocalDate.parse(displayDate, DateTimeFormatter.ofPattern(pattern))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
            }.getOrElse {
                // fallback: maybe user typed ISO directly
                runCatching {
                    LocalDate.parse(displayDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                }.getOrNull()
            }

        fun provideFactory(
            app: Application,
            workoutId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val repo = WorkoutRepository.create(app)
                val prefsRepo = UserPreferencesRepository(app)
                val catalogRepo = ExerciseCatalogRepository(app)
                @Suppress("UNCHECKED_CAST")
                return WorkoutEditViewModel(repo, prefsRepo, catalogRepo, workoutId) as T
            }
        }
    }
}
