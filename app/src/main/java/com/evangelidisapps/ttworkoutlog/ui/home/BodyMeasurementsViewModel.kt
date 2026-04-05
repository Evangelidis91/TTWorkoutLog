package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.evangelidisapps.ttworkoutlog.data.model.BodyMeasurement
import com.evangelidisapps.ttworkoutlog.data.repository.BodyMeasurementRepository
import com.evangelidisapps.ttworkoutlog.data.repository.UserPreferencesRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class MeasurementFormState(
    val dateLabel: String = "",
    val weight: String = "",
    val bodyFat: String = "",
    val muscleMass: String = "",
    val bmi: String = "",
    val waist: String = "",
    val hip: String = "",
    val chest: String = "",
    val arm: String = "",
    val thigh: String = "",
    val note: String = "",
    /** non-null means editing an existing entry */
    val editingId: String? = null
)

data class BodyMeasurementsUiState(
    val measurements: List<BodyMeasurement> = emptyList(),
    val showForm: Boolean = false,
    val form: MeasurementFormState = MeasurementFormState(),
    val isSaving: Boolean = false,
    val weightUnit: String = "kg"
)

class BodyMeasurementsViewModel(
    app: Application,
    private val repository: BodyMeasurementRepository,
    private val prefsRepository: UserPreferencesRepository
) : AndroidViewModel(app) {

    private val analytics = FirebaseAnalytics.getInstance(app)

    private val _state = MutableStateFlow(BodyMeasurementsUiState())
    val state = _state.asStateFlow()

    val weightUnit = prefsRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "kg")

    val dateFormat = prefsRepository.dateFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "dd/MM/yyyy")

    init {
        viewModelScope.launch {
            repository.observeAll().collect { list ->
                _state.update { it.copy(measurements = list) }
            }
        }
    }

    fun openAddForm() {
        viewModelScope.launch {
            val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
            _state.update {
                it.copy(
                    showForm = true,
                    form = MeasurementFormState(
                        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern(fmt))
                    )
                )
            }
        }
    }

    fun openEditForm(measurement: BodyMeasurement) {
        viewModelScope.launch {
            val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
            val unit = prefsRepository.weightUnit.firstOrNull() ?: "kg"
            _state.update {
                it.copy(
                    showForm = true,
                    form = MeasurementFormState(
                        editingId = measurement.id,
                        dateLabel = isoToDisplay(measurement.date, fmt),
                        weight = measurement.weightKg?.let { kg -> kgToDisplay(kg, unit) }.orEmpty(),
                        bodyFat = measurement.bodyFatPercent?.let { "%.1f".format(it) }.orEmpty(),
                        muscleMass = measurement.muscleMassKg?.let { kg -> kgToDisplay(kg, unit) }.orEmpty(),
                        bmi = measurement.bmi?.let { "%.1f".format(it) }.orEmpty(),
                        waist = measurement.waistCm?.let { "%.1f".format(it) }.orEmpty(),
                        hip = measurement.hipCm?.let { "%.1f".format(it) }.orEmpty(),
                        chest = measurement.chestCm?.let { "%.1f".format(it) }.orEmpty(),
                        arm = measurement.armCm?.let { "%.1f".format(it) }.orEmpty(),
                        thigh = measurement.thighCm?.let { "%.1f".format(it) }.orEmpty(),
                        note = measurement.note.orEmpty()
                    )
                )
            }
        }
    }

    fun closeForm() = _state.update { it.copy(showForm = false, form = MeasurementFormState()) }

    fun updateForm(transform: (MeasurementFormState) -> MeasurementFormState) =
        _state.update { it.copy(form = transform(it.form)) }

    fun save() {
        val form = _state.value.form
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val fmt = prefsRepository.dateFormat.firstOrNull() ?: "dd/MM/yyyy"
            val unit = prefsRepository.weightUnit.firstOrNull() ?: "kg"
            val isoDate = displayToIso(form.dateLabel, fmt)
                ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val id = form.editingId ?: UUID.randomUUID().toString()
            val weightKg = form.weight.toDoubleOrNull()?.let { displayToKg(it, unit) }
            val muscleMassKg = form.muscleMass.toDoubleOrNull()?.let { displayToKg(it, unit) }
            val fieldsFilledCount = listOf(
                form.weight, form.bodyFat, form.muscleMass, form.bmi,
                form.waist, form.hip, form.chest, form.arm, form.thigh
            ).count { it.isNotBlank() }
            repository.upsert(
                BodyMeasurement(
                    id = id,
                    date = isoDate,
                    weightKg = weightKg,
                    bodyFatPercent = form.bodyFat.toDoubleOrNull(),
                    muscleMassKg = muscleMassKg,
                    bmi = form.bmi.toDoubleOrNull(),
                    waistCm = form.waist.toDoubleOrNull(),
                    hipCm = form.hip.toDoubleOrNull(),
                    chestCm = form.chest.toDoubleOrNull(),
                    armCm = form.arm.toDoubleOrNull(),
                    thighCm = form.thigh.toDoubleOrNull(),
                    note = form.note.ifBlank { null },
                    createdAt = System.currentTimeMillis()
                )
            )
            analytics.logEvent("measurement_logged") {
                param("fields_filled", fieldsFilledCount.toLong())
                param("is_edit", if (form.editingId != null) 1L else 0L)
            }
            _state.update { it.copy(isSaving = false, showForm = false, form = MeasurementFormState()) }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    companion object {
        fun kgToDisplay(kg: Double, unit: String): String {
            val v = if (unit == "lbs") kg * 2.20462 else kg
            return if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
        }

        fun displayToKg(value: Double, unit: String): Double =
            if (unit == "lbs") value * 0.453592 else value

        fun isoToDisplay(iso: String, pattern: String): String =
            runCatching {
                LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern(pattern))
            }.getOrElse { iso }

        fun displayToIso(display: String, pattern: String): String? =
            runCatching {
                LocalDate.parse(display, DateTimeFormatter.ofPattern(pattern))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
            }.getOrElse {
                runCatching {
                    LocalDate.parse(display, DateTimeFormatter.ISO_LOCAL_DATE)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                }.getOrNull()
            }

        fun provideFactory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return BodyMeasurementsViewModel(
                        app,
                        BodyMeasurementRepository(app),
                        UserPreferencesRepository(app)
                    ) as T
                }
            }
    }
}
