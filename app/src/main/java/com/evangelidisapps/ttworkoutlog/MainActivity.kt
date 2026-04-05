package com.evangelidisapps.ttworkoutlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.evangelidisapps.ttworkoutlog.timer.TimerFinishedReceiver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimerFinishedReceiver.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            WorkoutApp()
        }
    }
}