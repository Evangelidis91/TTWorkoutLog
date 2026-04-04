package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutExercise
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutSet
import com.evangelidisapps.ttworkoutlog.data.model.WorkoutWithDetails

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutDetailScreen(
    workoutWithDetails: WorkoutWithDetails?,
    dateFormat: String = "dd/MM/yyyy",
    weightUnit: String = "kg",
    distanceUnit: String = "km",
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutWithDetails?.workout?.title ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (workoutWithDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Workout not found", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val workout = workoutWithDetails.workout

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header card ──────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDateDisplay(workout.dateLabel, dateFormat),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            workout.style?.let { style ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(style) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                            workout.durationSec?.let { sec ->
                                StatChip(label = formatDuration(sec))
                            }
                            workout.calories?.let { kcal ->
                                StatChip(label = "$kcal kcal")
                            }
                            val totalSets = workoutWithDetails.exercises.sumOf { it.sets.size }
                            if (totalSets > 0) {
                                StatChip(label = "$totalSets sets")
                            }
                            val exerciseCount = workoutWithDetails.exercises.size
                            if (exerciseCount > 0) {
                                StatChip(label = "$exerciseCount exercises")
                            }
                        }

                        workout.note?.let { note ->
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // ── Exercises ────────────────────────────────────────────────────
            if (workoutWithDetails.exercises.isEmpty()) {
                item {
                    Text(
                        text = "No exercises recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(workoutWithDetails.exercises) { exercise ->
                    ExerciseCard(exercise = exercise, weightUnit = weightUnit, distanceUnit = distanceUnit)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StatChip(label: String) {
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun ExerciseCard(exercise: WorkoutExercise, weightUnit: String = "kg", distanceUnit: String = "km") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    exercise.exercise.muscleGroup?.let { muscle ->
                        Text(
                            text = muscle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (exercise.isSuperset) {
                    SupersetBadge()
                }
            }

            exercise.note?.let { note ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exercise.sets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                var selectedSet by remember { mutableStateOf<WorkoutSet?>(null) }
                SetsTable(
                    sets = exercise.sets,
                    weightUnit = weightUnit,
                    distanceUnit = distanceUnit,
                    onSetTap = { set ->
                        if (set.weight != null && set.reps != null && set.reps > 0) {
                            selectedSet = set
                        }
                    }
                )
                selectedSet?.let { set ->
                    OneRepMaxBottomSheet(
                        set = set,
                        weightUnit = weightUnit,
                        onDismiss = { selectedSet = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun SupersetBadge() {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Superset",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
private fun SetsTable(
    sets: List<WorkoutSet>,
    weightUnit: String = "kg",
    distanceUnit: String = "km",
    onSetTap: (WorkoutSet) -> Unit = {}
) {
    val hasWeight = sets.any { it.weight != null }
    val hasReps = sets.any { it.reps != null }
    val hasDuration = sets.any { it.durationSec != null }
    val hasDistance = sets.any { it.distanceMeters != null }
    val has1RM = sets.any { it.weight != null && it.reps != null && it.reps > 0 }

    Column {
        // Table header
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell(text = "SET", weight = 0.12f, isHeader = true)
            TableCell(text = "TYPE", weight = 0.18f, isHeader = true)
            if (hasReps) TableCell(text = "REPS", weight = 0.2f, isHeader = true)
            if (hasWeight) TableCell(text = weightUnit.uppercase(), weight = 0.2f, isHeader = true)
            if (hasDuration) TableCell(text = "TIME", weight = 0.25f, isHeader = true)
            if (hasDistance) TableCell(text = distanceUnit.uppercase(), weight = 0.25f, isHeader = true)
            if (has1RM) TableCell(text = "1RM", weight = 0.2f, isHeader = true)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        sets.forEach { set ->
            val can1RM = set.weight != null && set.reps != null && set.reps > 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (can1RM) Modifier.clickable { onSetTap(set) }
                        else Modifier
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val setLabel = if (set.isWarmup) "W" else "${set.setNumber}"
                TableCell(
                    text = setLabel,
                    weight = 0.12f,
                    color = if (set.isWarmup)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                TableCell(
                    text = set.setType.replaceFirstChar { it.uppercase() },
                    weight = 0.18f,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasReps) TableCell(text = set.reps?.toString() ?: "—", weight = 0.2f)
                if (hasWeight) TableCell(text = set.weight?.let { formatWeight(it, weightUnit) } ?: "—", weight = 0.2f)
                if (hasDuration) TableCell(text = set.durationSec?.let { formatDuration(it) } ?: "—", weight = 0.25f)
                if (hasDistance) TableCell(text = set.distanceMeters?.let { formatDistance(it, distanceUnit) } ?: "—", weight = 0.25f)
                if (has1RM) {
                    if (can1RM) {
                        val epley = calc1RMEpley(set.weight!!, set.reps!!)
                        TableCell(
                            text = formatWeight(epley, weightUnit),
                            weight = 0.2f,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TableCell(text = "—", weight = 0.2f)
                    }
                }
            }
            set.note?.let { note ->
                Text(
                    text = "  ↳ $note",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        if (has1RM) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a set to see full 1RM breakdown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = if (isHeader) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else color
    )
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%d:%02d".format(m, s)
}

private fun formatWeight(kg: Double, unit: String = "kg"): String {
    val value = if (unit == "lbs") kg * 2.20462 else kg
    return if (value == value.toLong().toDouble()) value.toLong().toString() else "%.1f".format(value)
}

private fun formatDistance(meters: Int, unit: String = "km"): String {
    val value = if (unit == "miles") meters / 1609.344 else meters / 1000.0
    return "%.2f".format(value)
}

// ── 1RM Calculations ─────────────────────────────────────────────────────────

private fun calc1RMEpley(weightKg: Double, reps: Int): Double =
    weightKg * (1 + reps / 30.0)

private fun calc1RMBrzycki(weightKg: Double, reps: Int): Double =
    weightKg * 36.0 / (37.0 - reps)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OneRepMaxBottomSheet(
    set: WorkoutSet,
    weightUnit: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weight = set.weight!!
    val reps = set.reps!!

    val epley = calc1RMEpley(weight, reps)
    val brzycki = if (reps < 37) calc1RMBrzycki(weight, reps) else null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "1RM Estimate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Based on ${formatWeight(weight, weightUnit)} $weightUnit × $reps reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            OneRepMaxRow(
                formula = "Epley",
                description = "w × (1 + r ÷ 30)",
                value = formatWeight(epley, weightUnit),
                unit = weightUnit
            )

            if (brzycki != null) {
                OneRepMaxRow(
                    formula = "Brzycki",
                    description = "w × 36 ÷ (37 − r)",
                    value = formatWeight(brzycki, weightUnit),
                    unit = weightUnit
                )
            }

            if (reps > 10) {
                Text(
                    text = "Note: estimates are most accurate for 1–10 reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OneRepMaxRow(
    formula: String,
    description: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formula,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
