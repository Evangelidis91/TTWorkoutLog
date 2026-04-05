package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.evangelidisapps.ttworkoutlog.data.model.CatalogExercise
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
    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()
    val catalogFilter by viewModel.catalogFilter.collectAsStateWithLifecycle()
    val catalogResults by viewModel.catalogResults.collectAsStateWithLifecycle()
    val selectedExerciseDetail by viewModel.selectedExerciseDetail.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved(state.workoutId)
    }

    WorkoutEditContent(
        weightUnit = weightUnit,
        distanceUnit = distanceUnit,
        state = state,
        catalogFilter = catalogFilter,
        catalogResults = catalogResults,
        selectedExerciseDetail = selectedExerciseDetail,
        onTitleChange = viewModel::updateTitle,
        onNoteChange = viewModel::updateNote,
        onStyleChange = viewModel::updateStyle,
        onDurationChange = viewModel::updateDuration,
        onCaloriesChange = viewModel::updateCalories,
        onDateChange = viewModel::updateDateLabel,
        onOpenPicker = viewModel::openExercisePicker,
        onClosePicker = viewModel::closeExercisePicker,
        onViewDetail = viewModel::openExerciseDetail,
        onCloseDetail = viewModel::closeExerciseDetail,
        onSelectFromCatalog = viewModel::addExerciseFromCatalog,
        onCatalogQueryChange = viewModel::setCatalogQuery,
        onCatalogEquipmentChange = viewModel::setCatalogEquipment,
        onCatalogMuscleChange = viewModel::setCatalogMuscle,
        onCatalogCategoryChange = viewModel::setCatalogCategory,
        onClearCatalogFilters = viewModel::clearCatalogFilters,
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
    distanceUnit: String = "km",
    catalogFilter: CatalogFilter = CatalogFilter(),
    catalogResults: List<CatalogExercise> = emptyList(),
    selectedExerciseDetail: CatalogExercise? = null,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onViewDetail: (CatalogExercise) -> Unit,
    onCloseDetail: () -> Unit,
    onSelectFromCatalog: (CatalogExercise) -> Unit,
    onCatalogQueryChange: (String) -> Unit,
    onCatalogEquipmentChange: (String?) -> Unit,
    onCatalogMuscleChange: (String?) -> Unit,
    onCatalogCategoryChange: (String?) -> Unit,
    onClearCatalogFilters: () -> Unit,
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
    if (state.showExercisePicker) {
        ExercisePickerSheet(
            results = catalogResults,
            filter = catalogFilter,
            onQueryChange = onCatalogQueryChange,
            onEquipmentChange = onCatalogEquipmentChange,
            onMuscleChange = onCatalogMuscleChange,
            onCategoryChange = onCatalogCategoryChange,
            onClearFilters = onClearCatalogFilters,
            onViewDetail = onViewDetail,
            onDismiss = onClosePicker
        )
    }

    selectedExerciseDetail?.let { exercise ->
        ExerciseDetailSheet(
            exercise = exercise,
            onAddToWorkout = onSelectFromCatalog,
            onDismiss = onCloseDetail
        )
    }

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
            Button(onClick = onOpenPicker, enabled = !state.isSaving) {
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
                    distanceUnit = distanceUnit,
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

// ── Exercise picker ───────────────────────────────────────────────────────────

private val CATALOG_CATEGORIES = listOf(
    "cardio", "olympic weightlifting", "plyometrics", "powerlifting",
    "strength", "stretching", "strongman"
)
private val CATALOG_EQUIPMENT = listOf(
    "bands", "barbell", "body only", "cable", "dumbbell", "e-z curl bar",
    "exercise ball", "foam roll", "kettlebells", "machine", "medicine ball", "other"
)
private val CATALOG_MUSCLES = listOf(
    "abdominals", "abductors", "adductors", "biceps", "calves", "chest",
    "forearms", "glutes", "hamstrings", "lats", "lower back", "middle back",
    "neck", "quadriceps", "shoulders", "traps", "triceps"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    results: List<CatalogExercise>,
    filter: CatalogFilter,
    onQueryChange: (String) -> Unit,
    onEquipmentChange: (String?) -> Unit,
    onMuscleChange: (String?) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onViewDetail: (CatalogExercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Add Exercise",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = filter.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search exercises…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Filter chips ──────────────────────────────────────────────────
            val hasFilters = filter.equipment != null || filter.muscle != null || filter.category != null
            if (hasFilters) {
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Clear filters", color = MaterialTheme.colorScheme.error)
                }
            }

            // Category row
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CATALOG_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = filter.category == cat,
                        onClick = { onCategoryChange(if (filter.category == cat) null else cat) },
                        label = { Text(cat.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Equipment row
            Text(
                text = "Equipment",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CATALOG_EQUIPMENT.forEach { eq ->
                    FilterChip(
                        selected = filter.equipment == eq,
                        onClick = { onEquipmentChange(if (filter.equipment == eq) null else eq) },
                        label = { Text(eq.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Muscle row
            Text(
                text = "Muscle",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CATALOG_MUSCLES.forEach { muscle ->
                    FilterChip(
                        selected = filter.muscle == muscle,
                        onClick = { onMuscleChange(if (filter.muscle == muscle) null else muscle) },
                        label = { Text(muscle.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Results list ──────────────────────────────────────────────────
            if (results.isEmpty()) {
                Text(
                    text = "No exercises found. Try a different search or filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(results, key = { it.id }) { exercise ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (exercise.primaryMuscles.isNotEmpty()) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    exercise.primaryMuscles.first()
                                                        .replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                    exercise.equipment?.let { eq ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    eq.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { onViewDetail(exercise) }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "View details",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onViewDetail(exercise) }
                        )
                        HorizontalDivider()
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ── Exercise detail sheet ─────────────────────────────────────────────────────

private fun exerciseImageUrl(exerciseId: String): String =
    "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/$exerciseId/0.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailSheet(
    exercise: CatalogExercise,
    onAddToWorkout: (CatalogExercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Image ─────────────────────────────────────────────────────────
            item {
                AsyncImage(
                    model = exerciseImageUrl(exercise.id),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // ── Name + meta chips ─────────────────────────────────────────────
            item {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(exercise.level.replaceFirstChar { it.uppercase() }) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(exercise.category.replaceFirstChar { it.uppercase() }) }
                    )
                    exercise.equipment?.let { eq ->
                        AssistChip(
                            onClick = {},
                            label = { Text(eq.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // ── Primary muscles ───────────────────────────────────────────────
            if (exercise.primaryMuscles.isNotEmpty()) {
                item {
                    Text(
                        text = "Primary muscles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        exercise.primaryMuscles.forEach { muscle ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(muscle.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            // ── Secondary muscles ─────────────────────────────────────────────
            if (exercise.secondaryMuscles.isNotEmpty()) {
                item {
                    Text(
                        text = "Secondary muscles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        exercise.secondaryMuscles.forEach { muscle ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(muscle.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            // ── Instructions ──────────────────────────────────────────────────
            if (exercise.instructions.isNotEmpty()) {
                item {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                itemsIndexed(exercise.instructions) { index, step ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Add button ────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onAddToWorkout(exercise) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to Workout")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
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
    distanceUnit: String = "km",
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
                        distanceUnit = distanceUnit,
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
    distanceUnit: String = "km",
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
                    label = { Text("Distance ($distanceUnit)") },
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
            onOpenPicker = {},
            onClosePicker = {},
            onViewDetail = {},
            onCloseDetail = {},
            onSelectFromCatalog = {},
            onCatalogQueryChange = {},
            onCatalogEquipmentChange = {},
            onCatalogMuscleChange = {},
            onCatalogCategoryChange = {},
            onClearCatalogFilters = {},
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
