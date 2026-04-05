package com.evangelidisapps.ttworkoutlog.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.evangelidisapps.ttworkoutlog.MainActivity

const val TIMER_CHANNEL_ID = "rest_timer"

class TimerFinishedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureChannel(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val tapPi = PendingIntent.getActivity(
            context, 1, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Rest Timer")
            .setContentText("Time's up! Get back to it!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(tapPi)
            .build()

        NotificationManagerCompat.from(context).notify(TIMER_NOTIFICATION_ID, notification)
    }

    companion object {
        const val TIMER_NOTIFICATION_ID = 1001

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(TIMER_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    TIMER_CHANNEL_ID,
                    "Rest Timer",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when rest timer finishes"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
