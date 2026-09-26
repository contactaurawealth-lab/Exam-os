package com.studyoffline.app.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.model.SessionType
import com.studyoffline.app.data.model.Topic
import com.studyoffline.app.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class DayLoad {
    NONE, LIGHT, MEDIUM, HEAVY
}

data class CalendarDayItem(
    val date: LocalDate,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val load: DayLoad,
    val topicCount: Int,
    val isSelected: Boolean,
    val isToday: Boolean
)

data class PlannerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<CalendarDayItem> = emptyList(),
    val scheduledTopics: List<Topic> = emptyList(),
    val incompleteTopics: List<Topic> = emptyList(),
    val pomodoroMode: PomodoroMode = PomodoroMode.FOCUS,
    val pomodoroSecondsRemaining: Int = 25 * 60,
    val pomodoroTotalSeconds: Int = 25 * 60,
    val isPomodoroRunning: Boolean = false,
    val keepScreenOn: Boolean = false
)

enum class PomodoroMode(val title: String, val defaultMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _pomodoroMode = MutableStateFlow(PomodoroMode.FOCUS)
    private val _pomodoroSecondsRemaining = MutableStateFlow(25 * 60)
    private val _pomodoroTotalSeconds = MutableStateFlow(25 * 60)
    private val _isPomodoroRunning = MutableStateFlow(false)
    private val _keepScreenOn = MutableStateFlow(false)

    private var timerJob: Job? = null

    private val pomodoroStateFlow = combine(
        _pomodoroMode,
        _pomodoroSecondsRemaining,
        _isPomodoroRunning
    ) { mode, remaining, running ->
        Triple(mode, remaining, running)
    }

    val uiState: StateFlow<PlannerUiState> = combine(
        _selectedDate,
        studyRepository.getAllScheduledTopics(),
        studyRepository.getIncompleteTopics(),
        pomodoroStateFlow
    ) { date: LocalDate, scheduled: List<Topic>, incomplete: List<Topic>, pomodoro ->
        val (mode, remaining, running) = pomodoro
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        // Find Monday of the week containing selectedDate
        val startOfWeek = date.minusDays((date.dayOfWeek.value - 1).toLong())
        val daysOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }

        // Group scheduled topics by date
        val topicsByDate = scheduled.groupBy { topic ->
            if (topic.scheduledDate != null) {
                java.time.Instant.ofEpochMilli(topic.scheduledDate).atZone(zoneId).toLocalDate()
            } else null
        }

        val weekItems = daysOfWeek.map { d ->
            val count = topicsByDate[d]?.size ?: 0
            val load = when {
                count == 0 -> DayLoad.NONE
                count in 1..2 -> DayLoad.LIGHT
                count in 3..4 -> DayLoad.MEDIUM
                else -> DayLoad.HEAVY
            }
            CalendarDayItem(
                date = d,
                dayOfMonth = d.dayOfMonth,
                dayOfWeek = d.dayOfWeek.name.take(3),
                load = load,
                topicCount = count,
                isSelected = d == date,
                isToday = d == today
            )
        }

        val scheduledForSelectedDate = topicsByDate[date] ?: emptyList()

        PlannerUiState(
            selectedDate = date,
            weekDays = weekItems,
            scheduledTopics = scheduledForSelectedDate,
            incompleteTopics = incomplete.filter { it.scheduledDate == null },
            pomodoroMode = mode,
            pomodoroSecondsRemaining = remaining,
            pomodoroTotalSeconds = _pomodoroTotalSeconds.value,
            isPomodoroRunning = running,
            keepScreenOn = _keepScreenOn.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun assignTopicToDate(topicId: Long, date: LocalDate) {
        viewModelScope.launch {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            studyRepository.updateTopicScheduledDate(topicId, startOfDay)
        }
    }

    fun unassignTopic(topicId: Long) {
        viewModelScope.launch {
            studyRepository.updateTopicScheduledDate(topicId, null)
        }
    }

    fun toggleTopicCompletion(topicId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            studyRepository.toggleTopicCompletion(topicId, !currentCompleted)
        }
    }

    // ----------------------------------------------------
    // Pomodoro Timer Logic
    // ----------------------------------------------------
    fun setPomodoroMode(mode: PomodoroMode) {
        timerJob?.cancel()
        _isPomodoroRunning.value = false
        _pomodoroMode.value = mode
        val seconds = mode.defaultMinutes * 60
        _pomodoroSecondsRemaining.value = seconds
        _pomodoroTotalSeconds.value = seconds
    }

    fun togglePomodoro() {
        if (_isPomodoroRunning.value) {
            pausePomodoro()
        } else {
            startPomodoro()
        }
    }

    private fun startPomodoro() {
        _isPomodoroRunning.value = true
        timerJob = viewModelScope.launch {
            while (_pomodoroSecondsRemaining.value > 0) {
                delay(1000)
                _pomodoroSecondsRemaining.value -= 1
            }
            // Timer complete
            _isPomodoroRunning.value = false
            if (_pomodoroMode.value == PomodoroMode.FOCUS) {
                val focusDuration = _pomodoroTotalSeconds.value / 60
                studyRepository.recordStudySession(
                    topicId = null,
                    durationMinutes = focusDuration,
                    type = SessionType.POMODORO
                )
            }
        }
    }

    private fun pausePomodoro() {
        _isPomodoroRunning.value = false
        timerJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        val seconds = _pomodoroMode.value.defaultMinutes * 60
        _pomodoroSecondsRemaining.value = seconds
        _pomodoroTotalSeconds.value = seconds
    }

    fun toggleKeepScreenOn() {
        _keepScreenOn.value = !_keepScreenOn.value
    }
}
