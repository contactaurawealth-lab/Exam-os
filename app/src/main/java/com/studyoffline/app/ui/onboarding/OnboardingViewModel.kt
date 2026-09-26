package com.studyoffline.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.data.repository.StudyRepository
import com.studyoffline.app.ui.theme.SubjectTagPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class OnboardingSubjectItem(
    val name: String,
    val colorHex: String,
    val isSelected: Boolean
)

data class OnboardingState(
    val step: Int = 0, // 0: Welcome, 1: Goal, 2: Subjects, 3: Daily Time, 4: Notification, 5: Theme, 6: Ready
    val examName: String = "",
    val examGoalDate: Long? = null,
    val availableSubjects: List<OnboardingSubjectItem> = emptyList(),
    val customSubjectInput: String = "",
    val subjectError: String? = null,
    val dailyStudyMinutes: Int = 60,
    val customMinutesInput: String = "",
    val reminderTimeMinutes: Int = 18 * 60, // 6:00 PM
    val themeMode: String = "SYSTEM",
    val accentHex: String = "#7C9A82",
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val defaultSubjectNames = listOf(
        "Mathematics", "Physics", "Chemistry", "Biology",
        "Computer Science", "History", "Literature", "Psychology"
    )

    private val _state = MutableStateFlow(
        OnboardingState(
            availableSubjects = defaultSubjectNames.mapIndexed { index, name ->
                OnboardingSubjectItem(
                    name = name,
                    colorHex = SubjectTagPalette[index % SubjectTagPalette.size],
                    isSelected = index < 2 // Pre-select first 2 as friendly default
                )
            }
        )
    )
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextStep() {
        if (_state.value.step == 2) {
            // Validate at least one subject selected
            val hasSelection = _state.value.availableSubjects.any { it.isSelected }
            if (!hasSelection) {
                _state.update { it.copy(subjectError = "Pick at least one subject to continue") }
                return
            } else {
                _state.update { it.copy(subjectError = null) }
            }
        }
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(6)) }
    }

    fun previousStep() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun setExamGoal(name: String, dateMillis: Long?) {
        _state.update { it.copy(examName = name, examGoalDate = dateMillis) }
    }

    fun toggleSubject(name: String) {
        _state.update { current ->
            val updated = current.availableSubjects.map {
                if (it.name == name) it.copy(isSelected = !it.isSelected) else it
            }
            val hasSelection = updated.any { it.isSelected }
            current.copy(
                availableSubjects = updated,
                subjectError = if (hasSelection) null else current.subjectError
            )
        }
    }

    fun setCustomSubjectInput(input: String) {
        _state.update { it.copy(customSubjectInput = input) }
    }

    fun addCustomSubject() {
        val name = _state.value.customSubjectInput.trim()
        if (name.isEmpty()) return

        _state.update { current ->
            if (current.availableSubjects.any { it.name.equals(name, ignoreCase = true) }) {
                current.copy(customSubjectInput = "")
            } else {
                val color = SubjectTagPalette[current.availableSubjects.size % SubjectTagPalette.size]
                val newItem = OnboardingSubjectItem(name = name, colorHex = color, isSelected = true)
                current.copy(
                    availableSubjects = current.availableSubjects + newItem,
                    customSubjectInput = "",
                    subjectError = null
                )
            }
        }
    }

    fun setDailyStudyMinutes(minutes: Int) {
        _state.update { it.copy(dailyStudyMinutes = minutes) }
    }

    fun setCustomMinutes(minutesStr: String) {
        val min = minutesStr.toIntOrNull()
        if (min != null && min > 0) {
            _state.update { it.copy(dailyStudyMinutes = min, customMinutesInput = minutesStr) }
        } else {
            _state.update { it.copy(customMinutesInput = minutesStr) }
        }
    }

    fun setReminderTime(minutesOfDay: Int) {
        _state.update { it.copy(reminderTimeMinutes = minutesOfDay) }
    }

    fun setTheme(mode: String, accent: String) {
        _state.update { it.copy(themeMode = mode, accentHex = accent) }
    }

    fun skipSetup() {
        viewModelScope.launch {
            // Sensible defaults
            val defaultColor = SubjectTagPalette[0]
            studyRepository.insertSubject("General Studies", defaultColor)
            preferencesRepository.setDailyStudyMinutes(60)
            preferencesRepository.setDailyReminderTimeMinutes(18 * 60)
            preferencesRepository.setOnboardingComplete(true)
            _state.update { it.copy(isCompleted = true) }
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val currentState = _state.value
            val selected = currentState.availableSubjects.filter { it.isSelected }

            selected.forEach { item ->
                studyRepository.insertSubject(item.name, item.colorHex)
            }

            preferencesRepository.setExamGoal(currentState.examName, currentState.examGoalDate)
            preferencesRepository.setDailyStudyMinutes(currentState.dailyStudyMinutes)
            preferencesRepository.setDailyReminderTimeMinutes(currentState.reminderTimeMinutes)
            preferencesRepository.setThemeMode(currentState.themeMode)
            preferencesRepository.setAccentColor(currentState.accentHex)
            preferencesRepository.setOnboardingComplete(true)

            _state.update { it.copy(isCompleted = true) }
        }
    }
}
