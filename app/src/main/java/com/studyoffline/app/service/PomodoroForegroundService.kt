package com.studyoffline.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.studyoffline.app.MainActivity
import com.studyoffline.app.R
import com.studyoffline.app.notification.NotificationHelper

class PomodoroForegroundService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_SECONDS_REMAINING = "EXTRA_SECONDS_REMAINING"
        const val EXTRA_MODE_NAME = "EXTRA_MODE_NAME"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                val seconds = intent?.getIntExtra(EXTRA_SECONDS_REMAINING, 0) ?: 0
                val mode = intent?.getStringExtra(EXTRA_MODE_NAME) ?: "Focus"
                val notification = buildNotification(seconds, mode, isPaused = true)
                startForeground(NotificationHelper.NOTIFICATION_ID_POMODORO_ONGOING, notification)
            }
            ACTION_START -> {
                val seconds = intent?.getIntExtra(EXTRA_SECONDS_REMAINING, 25 * 60) ?: (25 * 60)
                val mode = intent?.getStringExtra(EXTRA_MODE_NAME) ?: "Focus"
                val notification = buildNotification(seconds, mode, isPaused = false)
                startForeground(NotificationHelper.NOTIFICATION_ID_POMODORO_ONGOING, notification)
            }
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(secondsRemaining: Int, modeName: String, isPaused: Boolean): Notification {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "pomodoro")
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            50,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action
        val stopIntent = Intent(this, PomodoroForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            51,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ONGOING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Study Timer: $modeName ${if (isPaused) "(Paused)" else ""}")
            .setContentText("$timeString remaining")
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }
}
