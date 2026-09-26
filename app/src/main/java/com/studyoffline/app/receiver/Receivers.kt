package com.studyoffline.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.notification.AlarmScheduler
import com.studyoffline.app.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            if (settings.masterNotificationEnabled && settings.dailyReminderEnabled) {
                NotificationHelper.showDailyReminder(context)
                // Re-arm for tomorrow (PRD §9.2)
                AlarmScheduler.scheduleDailyReminder(context, settings.dailyReminderTimeMinutes)
            }
        }
    }
}

class CountdownAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val examName = intent.getStringExtra("exam_name") ?: "Target Exam"
        val daysLeft = intent.getIntExtra("days_left", 1)
        NotificationHelper.showCountdownMilestone(context, examName, daysLeft)
    }
}

class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val modeName = intent.getStringExtra("mode_name") ?: "Focus"
        NotificationHelper.showPomodoroComplete(context, modeName)
    }
}

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            if (settings.masterNotificationEnabled && settings.dailyReminderEnabled) {
                AlarmScheduler.scheduleDailyReminder(context, settings.dailyReminderTimeMinutes)
            }
            if (settings.masterNotificationEnabled && settings.countdownMilestonesEnabled && settings.examGoalDate != null) {
                AlarmScheduler.scheduleCountdownMilestones(context, settings.examGoalDate, settings.examName)
            }
        }
    }
}
