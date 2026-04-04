package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evangelidisapps.ttworkoutlog.ui.theme.TTWorkoutLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    viewModel: WorkoutEditViewModel,
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved(state.workoutId)
    }

    WorkoutEditContent(
        weightUnit = weightUnit,
        state = state,
        onTitleChange = viewModel::updateTitle,
        onNoteChange = viewModel::updateNote,
        onStyleChange = viewModel::updateStyle,
        onDurationChange = viewModel::updateDuration,
        onCaloriesChange = viewModel::updateCalories,
        onDateChange = viewModel::updateDateLabel,
        onAddExercise = viewModel::addExercise,
        onRemoveExercise = viewModel::removeExercise,
        onExerciseNameChange = viewModel::updateExerciseName,
        onExerciseNoteChange = viewModel::updateExerciseNote,
        onExerciseMuscleChange = viewModel::updateExerciseMuscle,
        onSupersetToggle = viewModel::toggleSuperset,
        onAddSet = viewModel::addSet,
        onRemoveSet = viewModel::removeSet,
        onUpdateSet = viewModel::updateSet,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutEditContent(
    state: WorkoutEditState,
    weightUnit: String = "kg",
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (String) -> Unit,
    onExerciseNameChange: (String, String) -> Unit,
    onExerciseNoteChange: (String, String) -> Unit,
    onExerciseMuscleChange: (String, String) -> Unit,
    onSupersetToggle: (String, Boolean) -> Unit,
    onAddSet: (String) -> Unit,
    onRemoveSet: (String, String) -> Unit,
    onUpdateSet: (
        exerciseId: String,
        setId: String,
        reps: String?,
        weight: String?,
        durationSec: String?,
        distanceMeters: String?,
        calories: String?,
        note: String?,
        setType: String?,
        isWarmup: Boolean?
    ) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Workout") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    Button(onClick = onSave, enabled = !state.isSaving) {
                        Text(if (state.isSaving) "Saving..." else "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            Button(onClick = onAddExercise, enabled = !state.isSaving) {
                Text("Add Exercise")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Workout Info",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.dateLabel,
                    onValueChange = onDateChange,
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                StyleDropdown(
                    selected = state.style,
                    onStyleChange = onStyleChange
                )
                OutlinedTextField(
                    value = state.durationSec,
                    onValueChange = onDurationChange,
                    label = { Text("Duration (sec, optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.calories,
                    onValueChange = onCaloriesChange,
                    label = { Text("Calories (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items(state.exercises, key = { it.id }) { exercise ->
                ExerciseEditor(
                    exercise = exercise,
                    onRemove = { onRemoveExercise(exercise.id) },
                    onNameChange = { onExerciseNameChange(exercise.id, it) },
                    onNoteChange = { onExerciseNoteChange(exercise.id, it) },
                    onMuscleChange = { onExerciseMuscleChange(exercise.id, it) },
                    onSupersetToggle = { onSupersetToggle(exercise.id, it) },
                    weightUnit = weightUnit,
                    onAddSet = { onAddSet(exercise.id) },
                    onRemoveSet = { setId -> onRemoveSet(exercise.id, setId) },
                    onUpdateSet = { setId, reps, weight, duration, distance, calories, note, type, warmup ->
                        onUpdateSet(
                            exercise.id,
                            setId,
                            reps,
                            weight,
                            duration,
                            distance,
                            calories,
                            note,
                            type,
                            warmup
                        )
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}

private val workoutStyles = listOf("Standard", "WOD", "For Time", "AMRAP", "Complex", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleDropdown(
    selected: String,
    onStyleChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.ifBlank { "Standard" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Style") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            workoutStyles.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style) },
                    onClick = {
                        onStyleChange(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseEditor(
    exercise: EditableExerciseState,
    weightUnit: String = "kg",
    onRemove: () -> Unit,
    onNameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onMuscleChange: (String) -> Unit,
    onSupersetToggle: (Boolean) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (String) -> Unit,
    onUpdateSet: (
        setId: String,
        reps: String?,
        weight: String?,
        durationSec: String?,
        distanceMeters: String?,
        calories: String?,
        note: String?,
        setType: String?,
        isWarmup: Boolean?
    ) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Exercise", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onRemove) { Text("Remove") }
            }
            OutlinedTextField(
                value = exercise.name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                placeholder = { Text("e.g. Bench Press") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = exercise.muscleGroup,
                onValueChange = onMuscleChange,
                label = { Text("Muscle group (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = exercise.note,
                onValueChange = onNoteChange,
                label = { Text("Exercise note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Superset?")
                Switch(
                    checked = exercise.isSuperset,
                    onCheckedChange = onSupersetToggle
                )
            }
            Text("Sets", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                exercise.sets.forEach { set ->
                    SetEditor(
                        set = set,
                        weightUnit = weightUnit,
                        onRemove = { onRemoveSet(set.id) },
                        onUpdate = { reps, weight, duration, distance, calories, note, type, warmup ->
                            onUpdateSet(
                                set.id,
                                reps,
                                weight,
                                duration,
                                distance,
                                calories,
                                note,
                                type,
                                warmup
                            )
                        }
                    )
                }
            }
            TextButton(onClick = onAddSet) { Text("Add set") }
        }
    }
}

@Composable
private fun SetEditor(
    set: EditableSetState,
    weightUnit: String = "kg",
    onRemove: () -> Unit,
    onUpdate: (
        reps: String?,
        weight: String?,
        durationSec: String?,
        distanceMeters: String?,
        calories: String?,
        note: String?,
        setType: String?,
        isWarmup: Boolean?
    ) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove set"
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = set.reps,
                    onValueChange = { onUpdate(it, null, null, null, null, null, null, null) },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = set.weight,
                    onValueChange = { onUpdate(null, it, null, null, null, null, null, null) },
                    label = { Text("Weight ($weightUnit)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = set.durationSec,
                    onValueChange = { onUpdate(null, null, it, null, null, null, null, null) },
                    label = { Text("Time (sec)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = set.distanceMeters,
                    onValueChange = { onUpdate(null, null, null, it, null, null, null, null) },
                    label = { Text("Distance (m)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = set.calories,
                    onValueChange = { onUpdate(null, null, null, null, it, null, null, null) },
                    label = { Text("Calories") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = set.setType,
                    onValueChange = { onUpdate(null, null, null, null, null, null, it, null) },
                    label = { Text("Type (strength/time/etc)") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = set.note,
                onValueChange = { onUpdate(null, null, null, null, null, it, null, null) },
                label = { Text("Set note") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Warmup?")
                Switch(
                    checked = set.isWarmup,
                    onCheckedChange = { onUpdate(null, null, null, null, null, null, null, it) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutEditPreview() {
    val sampleExercise = EditableExerciseState(
        name = "Bench Press",
        sets = listOf(
            EditableSetState(setNumber = 1, reps = "8", weight = "60"),
            EditableSetState(setNumber = 2, reps = "8", weight = "60")
        )
    )
    TTWorkoutLogTheme {
        WorkoutEditContent(
            state = WorkoutEditState(
                title = "Push Day",
                exercises = listOf(sampleExercise)
            ),
            onTitleChange = {},
            onNoteChange = {},
            onStyleChange = {},
            onDurationChange = {},
            onCaloriesChange = {},
            onDateChange = {},
            onAddExercise = {},
            onRemoveExercise = {},
            onExerciseNameChange = { _, _ -> },
            onExerciseNoteChange = { _, _ -> },
            onExerciseMuscleChange = { _, _ -> },
            onSupersetToggle = { _, _ -> },
            onAddSet = {},
            onRemoveSet = { _, _ -> },
            onUpdateSet = { _, _, _, _, _, _, _, _, _, _ -> },
            onSave = {},
            onBack = {}
        )
    }
}
