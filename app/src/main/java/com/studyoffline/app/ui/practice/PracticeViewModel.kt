package com.studyoffline.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.model.Flashcard
import com.studyoffline.app.data.model.QuizQuestion
import com.studyoffline.app.data.model.SessionType
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeHomeUiState(
    val dueFlashcardCount: Int = 0,
    val weakQuestionCount: Int = 0,
    val totalQuestionCount: Int = 0,
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = true
)

data class QuizSessionState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerChecked: Boolean = false,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val isCompleted: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

data class FlashcardReviewState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val reviewedCount: Int = 0,
    val isCompleted: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    val homeUiState: StateFlow<PracticeHomeUiState> = combine(
        studyRepository.getDueFlashcardCount(),
        studyRepository.getWeakQuestionCount(),
        studyRepository.getAllSubjects(),
        studyRepository.getAllTopics()
    ) { dueCards, weakQuestions, subjects, topics ->
        PracticeHomeUiState(
            dueFlashcardCount = dueCards,
            weakQuestionCount = weakQuestions,
            totalQuestionCount = 0,
            subjects = subjects,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PracticeHomeUiState()
    )

    private val _quizState = MutableStateFlow(QuizSessionState())
    val quizState: StateFlow<QuizSessionState> = _quizState.asStateFlow()

    private val _flashcardState = MutableStateFlow(FlashcardReviewState())
    val flashcardState: StateFlow<FlashcardReviewState> = _flashcardState.asStateFlow()

    // ----------------------------------------------------
    // Quiz Setup & Session
    // ----------------------------------------------------
    fun startQuiz(subjectId: Long?, count: Int, weakOnly: Boolean = false) {
        _quizState.value = QuizSessionState(isLoading = true)
        viewModelScope.launch {
            val questions = if (weakOnly) {
                studyRepository.getWeakQuestionsList()
            } else if (subjectId != null) {
                val topics = studyRepository.getTopicsBySubject(subjectId).first()
                studyRepository.getQuestionsByTopicsList(topics.map { it.id })
            } else {
                val allTopics = studyRepository.getAllTopics().first()
                studyRepository.getQuestionsByTopicsList(allTopics.map { it.id })
            }

            val shuffled = questions.shuffled().take(if (count > 0) count else questions.size)
            _quizState.value = QuizSessionState(
                questions = shuffled,
                currentIndex = 0,
                selectedOptionIndex = null,
                isAnswerChecked = false,
                userAnswers = emptyMap(),
                isCompleted = false,
                startTime = System.currentTimeMillis(),
                isLoading = false
            )
        }
    }

    fun selectQuizOption(optionIndex: Int) {
        if (!_quizState.value.isAnswerChecked) {
            _quizState.update { it.copy(selectedOptionIndex = optionIndex) }
        }
    }

    fun checkQuizAnswer() {
        val current = _quizState.value
        if (current.selectedOptionIndex != null && !current.isAnswerChecked) {
            val updatedAnswers = current.userAnswers.toMutableMap()
            updatedAnswers[current.currentIndex] = current.selectedOptionIndex
            _quizState.update {
                it.copy(
                    isAnswerChecked = true,
                    userAnswers = updatedAnswers
                )
            }
        }
    }

    fun nextQuizQuestion() {
        val current = _quizState.value
        if (current.currentIndex + 1 < current.questions.size) {
            _quizState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerChecked = false
                )
            }
        } else {
            // Quiz completed
            val elapsedMinutes = ((System.currentTimeMillis() - current.startTime) / 60000L).toInt().coerceAtLeast(1)
            viewModelScope.launch {
                studyRepository.recordStudySession(
                    topicId = current.questions.firstOrNull()?.topicId,
                    durationMinutes = elapsedMinutes,
                    type = SessionType.QUIZ
                )
            }
            _quizState.update { it.copy(isCompleted = true) }
        }
    }

    fun toggleWeakStatus(questionId: Long, currentWeak: Boolean) {
        val newWeak = !currentWeak
        _quizState.update { state ->
            state.copy(
                questions = state.questions.map { q ->
                    if (q.id == questionId) q.copy(isWeak = newWeak) else q
                }
            )
        }
        viewModelScope.launch {
            studyRepository.setQuestionWeakStatus(questionId, newWeak)
        }
    }

    // ----------------------------------------------------
    // Flashcard Review (SM-2)
    // ----------------------------------------------------
    fun startFlashcardReview(topicId: Long? = null) {
        _flashcardState.value = FlashcardReviewState(isLoading = true)
        viewModelScope.launch {
            val cards = if (topicId != null) {
                studyRepository.getFlashcardsDueByTopic(topicId).first()
            } else {
                studyRepository.getDueFlashcardsList()
            }

            _flashcardState.value = FlashcardReviewState(
                cards = cards,
                currentIndex = 0,
                isFlipped = false,
                reviewedCount = 0,
                isCompleted = false,
                startTime = System.currentTimeMillis(),
                isLoading = false
            )
        }
    }

    fun flipFlashcard() {
        _flashcardState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun reviewFlashcard(grade: Int) {
        val current = _flashcardState.value
        val card = current.cards.getOrNull(current.currentIndex) ?: return

        viewModelScope.launch {
            studyRepository.reviewFlashcard(card, grade)

            if (current.currentIndex + 1 < current.cards.size) {
                _flashcardState.update {
                    it.copy(
                        currentIndex = it.currentIndex + 1,
                        isFlipped = false,
                        reviewedCount = it.reviewedCount + 1
                    )
                }
            } else {
                // Completed review session
                val elapsedMinutes = ((System.currentTimeMillis() - current.startTime) / 60000L).toInt().coerceAtLeast(1)
                studyRepository.recordStudySession(
                    topicId = card.topicId,
                    durationMinutes = elapsedMinutes,
                    type = SessionType.FLASHCARD
                )
                _flashcardState.update {
                    it.copy(
                        reviewedCount = it.reviewedCount + 1,
                        isCompleted = true
                    )
                }
            }
        }
    }
}
