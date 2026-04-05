package com.evangelidisapps.ttworkoutlog.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.evangelidisapps.ttworkoutlog.ui.theme.AppBottomSheet
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PRESETS = listOf(30 to "30s", 60 to "1m", 90 to "1.5m", 120 to "2m", 180 to "3m", 300 to "5m")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestTimerSheet(
    viewModel: RestTimerViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppBottomSheet(
        onDismissRequest = onDismiss
    ) {
        RestTimerContent(
            state = state,
            onStart = { viewModel.start(it) },
            onPause = { viewModel.pause() },
            onResume = { viewModel.resume() },
            onReset = { viewModel.reset() },
            onDismissFinished = { viewModel.dismissFinished() }
        )
    }
}

@Composable
private fun RestTimerContent(
    state: RestTimerUiState,
    onStart: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onDismissFinished: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Rest Timer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // ── Circular countdown ────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.size(160.dp),
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = if (state.isFinished) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
            )
            if (state.isFinished) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time's Up!", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error)
                    Text("Rest over", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(
                    text = if (state.totalSec > 0) state.formattedTime else "--:--",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Preset chips ─────────────────────────────────────────────────────
        AnimatedVisibility(visible = !state.isRunning && !state.isFinished) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PRESETS.forEach { (sec, label) ->
                    FilterChip(
                        selected = selectedPreset == sec,
                        onClick = { selectedPreset = sec },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Controls ─────────────────────────────────────────────────────────
        if (state.isFinished) {
            Button(
                onClick = onDismissFinished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss")
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Start / Pause / Resume
                Button(
                    onClick = {
                        when {
                            state.isRunning -> onPause()
                            state.remainingSec > 0 -> onResume()
                            else -> selectedPreset?.let { onStart(it) }
                        }
                    },
                    enabled = state.isRunning || state.remainingSec > 0 || selectedPreset != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            state.isRunning -> "Pause"
                            state.remainingSec > 0 -> "Resume"
                            else -> "Start"
                        }
                    )
                }

                // Reset
                OutlinedButton(
                    onClick = {
                        selectedPreset = null
                        onReset()
                    },
                    enabled = state.totalSec > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }

        Spacer(Modifier.height(0.dp)) // bottom padding handled by parent
    }
}
