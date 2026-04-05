package com.evangelidisapps.ttworkoutlog.timer

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestTimerUiState(
    val remainingSec: Int = 0,
    val totalSec: Int = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val showSheet: Boolean = false
) {
    val progress: Float get() = if (totalSec > 0) remainingSec.toFloat() / totalSec else 0f
    val formattedTime: String get() {
        val m = remainingSec / 60
        val s = remainingSec % 60
        return "%d:%02d".format(m, s)
    }
}

class RestTimerViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(RestTimerUiState())
    val state = _state.asStateFlow()
    private val analytics = FirebaseAnalytics.getInstance(app)

    private var countdownJob: Job? = null

    fun openSheet() = _state.update { it.copy(showSheet = true) }

    fun closeSheet() = _state.update { it.copy(showSheet = false) }

    /** Start a new countdown. Cancels any existing one. */
    fun start(durationSec: Int) {
        cancelAlarm()
        countdownJob?.cancel()
        analytics.logEvent("rest_timer_started") {
            param("duration_sec", durationSec.toLong())
        }
        _state.update { RestTimerUiState(
            remainingSec = durationSec,
            totalSec = durationSec,
            isRunning = true,
            showSheet = true
        ) }
        scheduleAlarm(durationSec)
        launchCountdown()
    }

    fun pause() {
        countdownJob?.cancel()
        cancelAlarm()
        _state.update { it.copy(isRunning = false) }
    }

    fun resume() {
        if (_state.value.isRunning || _state.value.remainingSec <= 0) return
        scheduleAlarm(_state.value.remainingSec)
        _state.update { it.copy(isRunning = true) }
        launchCountdown()
    }

    fun reset() {
        countdownJob?.cancel()
        cancelAlarm()
        _state.update { RestTimerUiState(showSheet = _state.value.showSheet) }
    }

    fun dismissFinished() {
        _state.update { it.copy(isFinished = false, remainingSec = 0, totalSec = 0, isRunning = false) }
    }

    private fun launchCountdown() {
        countdownJob = viewModelScope.launch {
            while (_state.value.remainingSec > 0) {
                delay(1_000L)
                _state.update { it.copy(remainingSec = it.remainingSec - 1) }
            }
            analytics.logEvent("rest_timer_completed") {
                param("duration_sec", _state.value.totalSec.toLong())
            }
            _state.update { it.copy(isRunning = false, isFinished = true) }
        }
    }

    private fun scheduleAlarm(delaySec: Int) {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = SystemClock.elapsedRealtime() + delaySec * 1_000L
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            alarmPendingIntent()
        )
    }

    private fun cancelAlarm() {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(app, TimerFinishedReceiver::class.java)
        return PendingIntent.getBroadcast(
            app, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onCleared() {
        super.onCleared()
        cancelAlarm()
        countdownJob?.cancel()
    }
}
