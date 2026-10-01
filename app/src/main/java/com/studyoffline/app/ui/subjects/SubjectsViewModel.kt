package com.studyoffline.app.ui.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.model.Flashcard
import com.studyoffline.app.data.model.QuizQuestion
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.data.model.Topic
import com.studyoffline.app.data.repository.StudyRepository
import com.studyoffline.app.data.repository.SubjectProgress
import com.studyoffline.app.ui.theme.SubjectTagPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectsUiState(
    val subjectsWithProgress: List<SubjectProgress> = emptyList(),
    val isLoading: Boolean = true
)

data class TopicDetailUiState(
    val topic: Topic? = null,
    val subject: Subject? = null,
    val flashcards: List<Flashcard> = emptyList(),
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val notesText: String = "",
    val isNotesSaved: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    val uiState: StateFlow<SubjectsUiState> = studyRepository.getSubjectProgress()
        .map { list ->
            SubjectsUiState(subjectsWithProgress = list, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubjectsUiState()
        )

    fun addSubject(name: String, colorHex: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (name.trim().isNotEmpty()) {
                studyRepository.insertSubject(name.trim(), colorHex)
                onComplete()
            }
        }
    }

    fun updateSubject(subject: Subject, onComplete: () -> Unit) {
        viewModelScope.launch {
            studyRepository.updateSubject(subject)
            onComplete()
        }
    }

    fun deleteSubject(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            studyRepository.deleteSubject(id)
            onComplete()
        }
    }

    // ----------------------------------------------------
    // Subject Detail (Topics for a Subject)
    // ----------------------------------------------------
    fun getTopicsForSubject(subjectId: Long): Flow<List<Topic>> {
        return studyRepository.getTopicsBySubject(subjectId)
    }

    fun getSubject(subjectId: Long): Flow<Subject?> {
        return studyRepository.getSubject(subjectId)
    }

    fun addTopic(subjectId: Long, name: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (name.trim().isNotEmpty()) {
                studyRepository.insertTopic(subjectId, name.trim())
                onComplete()
            }
        }
    }

    fun updateTopic(topic: Topic, onComplete: () -> Unit) {
        viewModelScope.launch {
            studyRepository.updateTopic(topic)
            onComplete()
        }
    }

    fun toggleTopicCompletion(topicId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            studyRepository.toggleTopicCompletion(topicId, !currentCompleted)
        }
    }

    fun deleteTopic(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            studyRepository.deleteTopic(id)
            onComplete()
        }
    }

    // ----------------------------------------------------
    // Topic Detail (Notes, Flashcards, Quiz)
    // ----------------------------------------------------
    fun getTopicDetail(topicId: Long): Flow<TopicDetailUiState> {
        return combine(
            studyRepository.getTopic(topicId),
            studyRepository.getFlashcardsByTopic(topicId),
            studyRepository.getQuestionsByTopic(topicId)
        ) { topic, cards, questions ->
            val subject = if (topic != null) studyRepository.getSubjectSync(topic.subjectId) else null
            TopicDetailUiState(
                topic = topic,
                subject = subject,
                flashcards = cards,
                quizQuestions = questions,
                notesText = topic?.notes ?: "",
                isNotesSaved = true,
                isLoading = false
            )
        }.flowOn(Dispatchers.IO)
    }

    fun saveNotes(topicId: Long, notes: String) {
        viewModelScope.launch {
            studyRepository.updateTopicNotes(topicId, notes)
        }
    }

    fun addFlashcard(topicId: Long, front: String, back: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (front.trim().isNotEmpty() && back.trim().isNotEmpty()) {
                studyRepository.insertFlashcard(topicId, front, back)
                onComplete()
            }
        }
    }

    fun deleteFlashcard(cardId: Long) {
        viewModelScope.launch {
            studyRepository.deleteFlashcard(cardId)
        }
    }

    fun addQuizQuestion(
        topicId: Long,
        question: String,
        options: List<String>,
        correctIndex: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (question.trim().isNotEmpty() && options.all { it.trim().isNotEmpty() }) {
                studyRepository.insertQuizQuestion(topicId, question, options, correctIndex)
                onComplete()
            }
        }
    }

    fun deleteQuizQuestion(questionId: Long) {
        viewModelScope.launch {
            studyRepository.deleteQuizQuestion(questionId)
        }
    }
}
