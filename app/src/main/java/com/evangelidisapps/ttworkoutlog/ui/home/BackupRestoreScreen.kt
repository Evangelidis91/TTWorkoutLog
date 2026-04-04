package com.evangelidisapps.ttworkoutlog.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = viewModel(),
    onBack: () -> Unit
) {
    val status by viewModel.status.collectAsStateWithLifecycle()

    // File picker to create a new .json file for export
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    // File picker to open an existing .json file for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    val defaultFilename = "tt_workout_backup_${
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }.json"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Export ───────────────────────────────────────────────────────
            BackupSection(
                title = "Export",
                description = "Save all your workouts to a JSON file. You can keep this as a backup or transfer it to another device.",
                actionLabel = "Export to JSON",
                onAction = { exportLauncher.launch(defaultFilename) },
                enabled = status !is BackupRestoreStatus.Loading
            )

            HorizontalDivider()

            // ── Import ───────────────────────────────────────────────────────
            BackupSection(
                title = "Restore",
                description = "Load workouts from a previously exported JSON file. Existing workouts with the same ID will be overwritten.",
                actionLabel = "Import from JSON",
                onAction = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                enabled = status !is BackupRestoreStatus.Loading,
                isDestructive = true
            )

            // ── Status ───────────────────────────────────────────────────────
            when (val s = status) {
                is BackupRestoreStatus.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Working…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is BackupRestoreStatus.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = s.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(onClick = viewModel::clearStatus) { Text("Dismiss") }
                    }
                }
                is BackupRestoreStatus.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = s.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(onClick = viewModel::clearStatus) { Text("Dismiss") }
                    }
                }
                BackupRestoreStatus.Idle -> {}
            }
        }
    }
}

@Composable
private fun BackupSection(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    enabled: Boolean,
    isDestructive: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onAction,
            enabled = enabled,
            colors = if (isDestructive) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) else ButtonDefaults.buttonColors()
        ) {
            Text(actionLabel)
        }
    }
}
