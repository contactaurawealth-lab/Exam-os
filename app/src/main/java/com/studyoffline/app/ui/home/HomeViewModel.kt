package com.studyoffline.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.preferences.UserSettings
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.data.repository.StudyRepository
import com.studyoffline.app.data.repository.TodayPlanItem
import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.domain.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "Hello",
    val streakInfo: StreakInfo = StreakInfo(0, 0, false),
    val examName: String = "",
    val daysRemaining: Long? = null,
    val todayPlanItems: List<TodayPlanItem> = emptyList(),
    val dueFlashcardCount: Int = 0,
    val subjectCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        preferencesRepository.userSettingsFlow,
        studyRepository.getStreakInfo(),
        studyRepository.getTodayPlanItems(limit = 3),
        studyRepository.getDueFlashcardCount(),
        studyRepository.getAllSubjects()
    ) { settings, streak, plan, dueCount, subjects ->
        val hour = java.time.LocalTime.now().hour
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }

        val daysRemaining = CountdownCalculator.calculateDaysRemaining(settings.examGoalDate)

        HomeUiState(
            greeting = greeting,
            streakInfo = streak,
            examName = settings.examName,
            daysRemaining = daysRemaining,
            todayPlanItems = plan,
            dueFlashcardCount = dueCount,
            subjectCount = subjects.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleTopicCompletion(topicId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            studyRepository.toggleTopicCompletion(topicId, !currentCompleted)
        }
    }
}
