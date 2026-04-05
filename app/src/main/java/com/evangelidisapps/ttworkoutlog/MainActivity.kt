package com.evangelidisapps.ttworkoutlog

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.evangelidisapps.ttworkoutlog.timer.TimerFinishedReceiver

private const val SPLASH_DURATION_MS = 3_000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStart = SystemClock.elapsedRealtime()
        installSplashScreen().setKeepOnScreenCondition {
            SystemClock.elapsedRealtime() - splashStart < SPLASH_DURATION_MS
        }
        super.onCreate(savedInstanceState)
        TimerFinishedReceiver.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            WorkoutApp()
        }
    }
}