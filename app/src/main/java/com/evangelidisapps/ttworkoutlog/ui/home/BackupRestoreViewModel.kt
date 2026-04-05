package com.evangelidisapps.ttworkoutlog.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidisapps.ttworkoutlog.data.repository.BackupRestoreRepository
import com.evangelidisapps.ttworkoutlog.data.repository.WorkoutRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BackupRestoreStatus {
    data object Idle : BackupRestoreStatus
    data object Loading : BackupRestoreStatus
    data class Success(val message: String) : BackupRestoreStatus
    data class Error(val message: String) : BackupRestoreStatus
}

class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BackupRestoreRepository(
        workoutRepository = WorkoutRepository.create(application),
        contentResolver = application.contentResolver
    )
    private val analytics = FirebaseAnalytics.getInstance(application)

    private val _status = MutableStateFlow<BackupRestoreStatus>(BackupRestoreStatus.Idle)
    val status = _status.asStateFlow()

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _status.update { BackupRestoreStatus.Loading }
            runCatching { repo.exportToJson(uri) }
                .onSuccess { count ->
                    analytics.logEvent("backup_exported") {
                        param("workout_count", count.toLong())
                    }
                    _status.update {
                        BackupRestoreStatus.Success("Exported $count workout${if (count == 1) "" else "s"} successfully")
                    }
                }
                .onFailure { e ->
                    _status.update {
                        BackupRestoreStatus.Error(e.localizedMessage ?: "Export failed")
                    }
                }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _status.update { BackupRestoreStatus.Loading }
            runCatching { repo.importFromJson(uri) }
                .onSuccess { count ->
                    analytics.logEvent("backup_imported") {
                        param("workout_count", count.toLong())
                    }
                    _status.update {
                        BackupRestoreStatus.Success("Restored $count workout${if (count == 1) "" else "s"} successfully")
                    }
                }
                .onFailure { e ->
                    _status.update {
                        BackupRestoreStatus.Error(e.localizedMessage ?: "Import failed")
                    }
                }
        }
    }

    fun clearStatus() {
        _status.update { BackupRestoreStatus.Idle }
    }
}
