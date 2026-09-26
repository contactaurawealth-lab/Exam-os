package com.studyoffline.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studyoffline.app.MainActivity
import com.studyoffline.app.R

object NotificationHelper {
    const val CHANNEL_DAILY_REMINDER = "channel_daily_reminder"
    const val CHANNEL_REVIEW_DUE = "channel_review_due"
    const val CHANNEL_COUNTDOWN = "channel_countdown"
    const val CHANNEL_POMODORO = "channel_pomodoro"
    const val CHANNEL_ONGOING = "channel_ongoing"

    const val NOTIFICATION_ID_DAILY = 1001
    const val NOTIFICATION_ID_REVIEW = 1002
    const val NOTIFICATION_ID_COUNTDOWN = 1003
    const val NOTIFICATION_ID_POMODORO_END = 1004
    const val NOTIFICATION_ID_POMODORO_ONGOING = 1005

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "Daily Study Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle daily reminders to keep your study streak active."
            }

            val reviewChannel = NotificationChannel(
                CHANNEL_REVIEW_DUE,
                "Spaced Repetition Due",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when flashcards are due for spaced repetition review."
            }

            val countdownChannel = NotificationChannel(
                CHANNEL_COUNTDOWN,
                "Exam Countdown Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Milestone alerts at 7, 3, and 1 day before your target exam."
            }

            val pomodoroChannel = NotificationChannel(
                CHANNEL_POMODORO,
                "Pomodoro Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Audible alerts when focus sessions or breaks end."
                enableVibration(true)
            }

            val ongoingChannel = NotificationChannel(
                CHANNEL_ONGOING,
                "Ongoing Session Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent, non-dismissible notification during active study timers."
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(dailyChannel, reviewChannel, countdownChannel, pomodoroChannel, ongoingChannel)
            )
        }
    }

    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("StudyOffline")
            .setContentText("Time for today's study session")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
    }

    fun showReviewDueNotification(context: Context, dueCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "flashcard_review")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REVIEW_DUE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Flashcards Due for Review")
            .setContentText("$dueCount card${if (dueCount == 1) "" else "s"} ready for spaced repetition")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REVIEW, notification)
    }

    fun showCountdownMilestone(context: Context, examName: String, daysLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "countdown_detail")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COUNTDOWN)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (examName.isNotEmpty()) examName else "Exam Countdown")
            .setContentText("$daysLeft day${if (daysLeft == 1) "" else "s"} remaining until your target date")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_COUNTDOWN, notification)
    }

    fun showPomodoroComplete(context: Context, modeName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "pomodoro")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Timer Complete")
            .setContentText("$modeName session finished. Great work!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_POMODORO_END, notification)
    }
}
