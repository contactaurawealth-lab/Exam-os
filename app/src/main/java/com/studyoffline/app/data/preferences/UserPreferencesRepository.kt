package com.studyoffline.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_offline_preferences")

data class UserSettings(
    val onboardingComplete: Boolean = false,
    val examGoalDate: Long? = null,
    val examName: String = "",
    val dailyStudyMinutes: Int = 60,
    val dailyReminderTimeMinutes: Int = 18 * 60, // 6:00 PM (1080 min)
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val accentColorHex: String = "#7C9A82",
    val masterNotificationEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val reviewDueEnabled: Boolean = true,
    val countdownMilestonesEnabled: Boolean = true,
    val pomodoroAlertsEnabled: Boolean = true,
    val ongoingSessionNotificationEnabled: Boolean = true,
    val persistentCountdownNotificationEnabled: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val EXAM_GOAL_DATE = longPreferencesKey("exam_goal_date")
        val EXAM_NAME = stringPreferencesKey("exam_name")
        val DAILY_STUDY_MINUTES = intPreferencesKey("daily_study_minutes")
        val DAILY_REMINDER_TIME_MINUTES = intPreferencesKey("daily_reminder_time_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val MASTER_NOTIFICATION_ENABLED = booleanPreferencesKey("master_notification_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REVIEW_DUE_ENABLED = booleanPreferencesKey("review_due_enabled")
        val COUNTDOWN_MILESTONES_ENABLED = booleanPreferencesKey("countdown_milestones_enabled")
        val POMODORO_ALERTS_ENABLED = booleanPreferencesKey("pomodoro_alerts_enabled")
        val ONGOING_SESSION_NOTIFICATION_ENABLED = booleanPreferencesKey("ongoing_session_notification_enabled")
        val PERSISTENT_COUNTDOWN_NOTIFICATION_ENABLED = booleanPreferencesKey("persistent_countdown_notification_enabled")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserSettings(preferences)
        }

    private fun mapUserSettings(preferences: Preferences): UserSettings {
        val examGoalDate = if (preferences.contains(PreferencesKeys.EXAM_GOAL_DATE)) {
            val date = preferences[PreferencesKeys.EXAM_GOAL_DATE] ?: -1L
            if (date > 0) date else null
        } else null

        return UserSettings(
            onboardingComplete = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
            examGoalDate = examGoalDate,
            examName = preferences[PreferencesKeys.EXAM_NAME] ?: "",
            dailyStudyMinutes = preferences[PreferencesKeys.DAILY_STUDY_MINUTES] ?: 60,
            dailyReminderTimeMinutes = preferences[PreferencesKeys.DAILY_REMINDER_TIME_MINUTES] ?: (18 * 60),
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            accentColorHex = preferences[PreferencesKeys.ACCENT_COLOR_HEX] ?: "#7C9A82",
            masterNotificationEnabled = preferences[PreferencesKeys.MASTER_NOTIFICATION_ENABLED] ?: true,
            dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: true,
            reviewDueEnabled = preferences[PreferencesKeys.REVIEW_DUE_ENABLED] ?: true,
            countdownMilestonesEnabled = preferences[PreferencesKeys.COUNTDOWN_MILESTONES_ENABLED] ?: true,
            pomodoroAlertsEnabled = preferences[PreferencesKeys.POMODORO_ALERTS_ENABLED] ?: true,
            ongoingSessionNotificationEnabled = preferences[PreferencesKeys.ONGOING_SESSION_NOTIFICATION_ENABLED] ?: true,
            persistentCountdownNotificationEnabled = preferences[PreferencesKeys.PERSISTENT_COUNTDOWN_NOTIFICATION_ENABLED] ?: false
        )
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setExamGoal(examName: String, goalDate: Long?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXAM_NAME] = examName
            if (goalDate != null && goalDate > 0) {
                preferences[PreferencesKeys.EXAM_GOAL_DATE] = goalDate
            } else {
                preferences.remove(PreferencesKeys.EXAM_GOAL_DATE)
            }
        }
    }

    suspend fun setDailyStudyMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_STUDY_MINUTES] = minutes }
    }

    suspend fun setDailyReminderTimeMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_REMINDER_TIME_MINUTES] = minutes }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setAccentColor(hex: String) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_COLOR_HEX] = hex }
    }

    suspend fun setMasterNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MASTER_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReviewDueEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.REVIEW_DUE_ENABLED] = enabled }
    }

    suspend fun setCountdownMilestonesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.COUNTDOWN_MILESTONES_ENABLED] = enabled }
    }

    suspend fun setPomodoroAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.POMODORO_ALERTS_ENABLED] = enabled }
    }

    suspend fun setOngoingSessionNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONGOING_SESSION_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setPersistentCountdownNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PERSISTENT_COUNTDOWN_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
