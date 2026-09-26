package com.studyoffline.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.backup.BackupManager
import com.studyoffline.app.data.backup.BackupRoot
import com.studyoffline.app.data.backup.ImportValidationResult
import com.studyoffline.app.data.preferences.UserSettings
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.data.repository.StudyRepository
import com.studyoffline.app.data.repository.SubjectProgress
import com.studyoffline.app.domain.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProgressDashboardUiState(
    val streakInfo: StreakInfo = StreakInfo(0, 0, false),
    val weeklyMinutes: Map<LocalDate, Int> = emptyMap(),
    val totalWeeklyMinutes: Int = 0,
    val subjectProgressList: List<SubjectProgress> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val progressDashboardState: StateFlow<ProgressDashboardUiState> = combine(
        studyRepository.getStreakInfo(),
        studyRepository.getWeeklyStudyMinutes(),
        studyRepository.getSubjectProgress()
    ) { streak, weekly, subjects ->
        ProgressDashboardUiState(
            streakInfo = streak,
            weeklyMinutes = weekly,
            totalWeeklyMinutes = weekly.values.sum(),
            subjectProgressList = subjects,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressDashboardUiState()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            preferencesRepository.setAccentColor(hex)
        }
    }

    fun setMasterNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMasterNotificationEnabled(enabled)
        }
    }

    fun setDailyReminder(enabled: Boolean, minutes: Int? = null) {
        viewModelScope.launch {
            preferencesRepository.setDailyReminderEnabled(enabled)
            if (minutes != null) {
                preferencesRepository.setDailyReminderTimeMinutes(minutes)
            }
        }
    }

    fun setReviewDue(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReviewDueEnabled(enabled)
        }
    }

    fun setCountdownMilestones(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCountdownMilestonesEnabled(enabled)
        }
    }

    fun setPomodoroAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPomodoroAlertsEnabled(enabled)
        }
    }

    fun setOngoingSessionNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOngoingSessionNotificationEnabled(enabled)
        }
    }

    fun setPersistentCountdown(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPersistentCountdownNotificationEnabled(enabled)
        }
    }

    // ----------------------------------------------------
    // Export / Import / Reset
    // ----------------------------------------------------
    fun exportBackup(uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.writeBackupToUri(uri)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Export failed.")
            }
        }
    }

    suspend fun validateImportFile(uri: Uri): ImportValidationResult {
        return backupManager.validateBackup(uri)
    }

    fun executeImport(root: BackupRoot, isReplace: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.executeImport(root, isReplace)
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Import failed.")
            }
        }
    }

    fun resetApp(onComplete: () -> Unit) {
        viewModelScope.launch {
            backupManager.resetApp()
            onComplete()
        }
    }
}
