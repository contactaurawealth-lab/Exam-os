package com.studyoffline.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.studyoffline.app.receiver.CountdownAlarmReceiver
import com.studyoffline.app.receiver.DailyReminderReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmScheduler {

    fun scheduleDailyReminder(context: Context, reminderTimeMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)
        val targetHour = reminderTimeMinutes / 60
        val targetMinute = reminderTimeMinutes % 60
        var targetTime = now.with(LocalTime.of(targetHour, targetMinute, 0))

        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val triggerAtMillis = targetTime.toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleCountdownMilestones(context: Context, examGoalDateMillis: Long?, examName: String) {
        if (examGoalDateMillis == null || examGoalDateMillis <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        // 7 days, 3 days, 1 day milestones (PRD §9.4)
        val milestones = listOf(7, 3, 1)
        milestones.forEachIndexed { index, daysBefore ->
            val triggerTime = examGoalDateMillis - (daysBefore * 86_400_000L)
            if (triggerTime > now) {
                val intent = Intent(context, CountdownAlarmReceiver::class.java).apply {
                    putExtra("exam_name", examName)
                    putExtra("days_left", daysBefore)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    200 + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } catch (e: Exception) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }
    }
}
