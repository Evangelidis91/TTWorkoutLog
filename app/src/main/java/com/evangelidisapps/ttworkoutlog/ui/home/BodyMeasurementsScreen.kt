package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evangelidisapps.ttworkoutlog.data.model.BodyMeasurement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: BodyMeasurementsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddForm() }) {
                Icon(Icons.Default.Add, contentDescription = "Log measurement")
            }
        }
    ) { innerPadding ->
        if (state.measurements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No measurements yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap + to log your first entry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                // Latest summary card
                item {
                    LatestMeasurementCard(
                        measurement = state.measurements.first(),
                        weightUnit = weightUnit,
                        dateFormat = dateFormat
                    )
                }
                // Progress charts (needs ≥ 2 entries with data)
                if (state.measurements.size >= 2) {
                    item {
                        ProgressChartsCard(
                            measurements = state.measurements,
                            weightUnit = weightUnit,
                            dateFormat = dateFormat
                        )
                    }
                }
                if (state.measurements.size > 1) {
                    item {
                        Text(
                            "History",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(state.measurements.drop(1), key = { it.id }) { m ->
                        MeasurementHistoryCard(
                            measurement = m,
                            weightUnit = weightUnit,
                            dateFormat = dateFormat,
                            onEdit = { viewModel.openEditForm(m) },
                            onDelete = { viewModel.delete(m.id) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (state.showForm) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeForm() },
            sheetState = sheetState
        ) {
            MeasurementForm(
                form = state.form,
                weightUnit = weightUnit,
                isSaving = state.isSaving,
                onUpdate = { viewModel.updateForm(it) },
                onSave = { viewModel.save() },
                onCancel = { viewModel.closeForm() }
            )
        }
    }
}

@Composable
private fun LatestMeasurementCard(
    measurement: BodyMeasurement,
    weightUnit: String,
    dateFormat: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Latest",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    BodyMeasurementsViewModel.isoToDisplay(measurement.date, dateFormat),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(12.dp))
            MeasurementGrid(measurement = measurement, weightUnit = weightUnit, prominent = true)
            measurement.note?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun MeasurementHistoryCard(
    measurement: BodyMeasurement,
    weightUnit: String,
    dateFormat: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    BodyMeasurementsViewModel.isoToDisplay(measurement.date, dateFormat),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            MeasurementGrid(measurement = measurement, weightUnit = weightUnit, prominent = false)
            measurement.note?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MeasurementGrid(
    measurement: BodyMeasurement,
    weightUnit: String,
    prominent: Boolean
) {
    val valueStyle = if (prominent) MaterialTheme.typography.titleLarge
                     else MaterialTheme.typography.bodyLarge
    val labelStyle = MaterialTheme.typography.labelSmall

    val items = buildList {
        measurement.weightKg?.let {
            add("Weight" to "${BodyMeasurementsViewModel.kgToDisplay(it, weightUnit)} $weightUnit")
        }
        measurement.bodyFatPercent?.let { add("Body Fat" to "%.1f%%".format(it)) }
        measurement.muscleMassKg?.let {
            add("Muscle" to "${BodyMeasurementsViewModel.kgToDisplay(it, weightUnit)} $weightUnit")
        }
        measurement.bmi?.let { add("BMI" to "%.1f".format(it)) }
        measurement.waistCm?.let { add("Waist" to "%.1f cm".format(it)) }
        measurement.hipCm?.let { add("Hip" to "%.1f cm".format(it)) }
        measurement.chestCm?.let { add("Chest" to "%.1f cm".format(it)) }
        measurement.armCm?.let { add("Arm" to "%.1f cm".format(it)) }
        measurement.thighCm?.let { add("Thigh" to "%.1f cm".format(it)) }
    }

    if (items.isEmpty()) {
        Text("No data recorded", style = labelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    // Two-column grid
    val pairs = items.chunked(2)
    pairs.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            row.forEach { (label, value) ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = labelStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = valueStyle, fontWeight = FontWeight.Medium)
                }
            }
            // If odd item in last row, fill the second slot
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MeasurementForm(
    form: MeasurementFormState,
    weightUnit: String,
    isSaving: Boolean,
    onUpdate: ((MeasurementFormState) -> MeasurementFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (form.editingId == null) "Log Measurement" else "Edit Measurement",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = form.dateLabel,
            onValueChange = { v -> onUpdate { it.copy(dateLabel = v) } },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()
        Text("Body Composition", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = form.weight,
                label = "Weight ($weightUnit)",
                modifier = Modifier.weight(1f),
                onChange = { v -> onUpdate { it.copy(weight = v) } }
            )
            NumberField(
                value = form.bodyFat,
                label = "Body Fat (%)",
                modifier = Modifier.weight(1f),
                onChange = { v -> onUpdate { it.copy(bodyFat = v) } }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = form.muscleMass,
                label = "Muscle Mass ($weightUnit)",
                modifier = Modifier.weight(1f),
                onChange = { v -> onUpdate { it.copy(muscleMass = v) } }
            )
            NumberField(
                value = form.bmi,
                label = "BMI",
                modifier = Modifier.weight(1f),
                onChange = { v -> onUpdate { it.copy(bmi = v) } }
            )
        }

        HorizontalDivider()
        Text("Circumferences (cm)", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(value = form.waist, label = "Waist",
                modifier = Modifier.weight(1f)) { v -> onUpdate { it.copy(waist = v) } }
            NumberField(value = form.hip, label = "Hip",
                modifier = Modifier.weight(1f)) { v -> onUpdate { it.copy(hip = v) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(value = form.chest, label = "Chest",
                modifier = Modifier.weight(1f)) { v -> onUpdate { it.copy(chest = v) } }
            NumberField(value = form.arm, label = "Arm",
                modifier = Modifier.weight(1f)) { v -> onUpdate { it.copy(arm = v) } }
        }
        NumberField(
            value = form.thigh,
            label = "Thigh",
            modifier = Modifier.fillMaxWidth(0.5f),
            onChange = { v -> onUpdate { it.copy(thigh = v) } }
        )

        HorizontalDivider()
        OutlinedTextField(
            value = form.note,
            onValueChange = { v -> onUpdate { it.copy(note = v) } },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onSave, enabled = !isSaving) {
                Text(if (form.editingId == null) "Save" else "Update")
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}
