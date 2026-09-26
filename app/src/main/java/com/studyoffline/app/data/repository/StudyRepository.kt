package com.studyoffline.app.data.repository

import com.studyoffline.app.data.local.*
import com.studyoffline.app.data.model.*
import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.domain.Sm2ReviewResult
import com.studyoffline.app.domain.SpacedRepetition
import com.studyoffline.app.domain.StreakCalculator
import com.studyoffline.app.domain.StreakInfo
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class SubjectWithTopics(
    val subject: Subject,
    val topics: List<Topic> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercent: Float = 0f
)

data class SubjectProgress(
    val subject: Subject,
    val completedTopics: Int,
    val totalTopics: Int,
    val progressPercent: Float
)

data class TodayPlanItem(
    val topic: Topic,
    val subjectName: String,
    val subjectColorHex: String
)

@Singleton
class StudyRepository @Inject constructor(
    private val database: AppDatabase,
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val flashcardDao: FlashcardDao,
    private val quizQuestionDao: QuizQuestionDao,
    private val studySessionDao: StudySessionDao
) {
    // ----------------------------------------------------
    // Subjects
    // ----------------------------------------------------
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun getAllSubjectsList(): List<Subject> = subjectDao.getAllSubjectsList()

    fun getSubject(id: Long): Flow<Subject?> = subjectDao.getSubjectById(id)

    suspend fun getSubjectSync(id: Long): Subject? = subjectDao.getSubjectByIdSync(id)

    suspend fun insertSubject(name: String, colorHex: String): Result<Long> = runCatching {
        val subject = Subject(name = name.trim(), colorHex = colorHex)
        subjectDao.insert(subject)
    }

    suspend fun updateSubject(subject: Subject): Result<Unit> = runCatching {
        subjectDao.update(subject.copy(name = subject.name.trim()))
    }

    suspend fun deleteSubject(id: Long): Result<Unit> = runCatching {
        subjectDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Topics
    // ----------------------------------------------------
    fun getTopicsBySubject(subjectId: Long): Flow<List<Topic>> = topicDao.getTopicsBySubject(subjectId)

    fun getAllTopics(): Flow<List<Topic>> = topicDao.getAllTopics()

    fun getAllScheduledTopics(): Flow<List<Topic>> = topicDao.getAllScheduledTopics()

    fun getIncompleteTopics(): Flow<List<Topic>> = topicDao.getIncompleteTopics()

    fun getTopic(id: Long): Flow<Topic?> = topicDao.getTopicById(id)

    suspend fun getTopicSync(id: Long): Topic? = topicDao.getTopicByIdSync(id)

    suspend fun insertTopic(subjectId: Long, name: String, notes: String? = null): Result<Long> = runCatching {
        val topic = Topic(
            subjectId = subjectId,
            name = name.trim(),
            notes = notes?.trim()
        )
        topicDao.insert(topic)
    }

    suspend fun updateTopic(topic: Topic): Result<Unit> = runCatching {
        topicDao.update(topic.copy(name = topic.name.trim()))
    }

    suspend fun toggleTopicCompletion(id: Long, isCompleted: Boolean): Result<Unit> = runCatching {
        topicDao.updateCompletion(id, isCompleted)
    }

    suspend fun updateTopicScheduledDate(id: Long, date: Long?): Result<Unit> = runCatching {
        topicDao.updateScheduledDate(id, date)
    }

    suspend fun updateTopicNotes(id: Long, notes: String?): Result<Unit> = runCatching {
        topicDao.updateNotes(id, notes)
    }

    suspend fun deleteTopic(id: Long): Result<Unit> = runCatching {
        topicDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Flashcards & Spaced Repetition (SM-2)
    // ----------------------------------------------------
    fun getFlashcardsByTopic(topicId: Long): Flow<List<Flashcard>> = flashcardDao.getFlashcardsByTopic(topicId)

    fun getDueFlashcards(now: Long = System.currentTimeMillis()): Flow<List<Flashcard>> =
        flashcardDao.getDueFlashcards(now)

    suspend fun getDueFlashcardsList(now: Long = System.currentTimeMillis()): List<Flashcard> =
        flashcardDao.getDueFlashcardsList(now)

    fun getDueFlashcardCount(now: Long = System.currentTimeMillis()): Flow<Int> =
        flashcardDao.getDueFlashcardCount(now)

    suspend fun getDueFlashcardCountSync(now: Long = System.currentTimeMillis()): Int =
        flashcardDao.getDueFlashcardCountSync(now)

    fun getFlashcardsDueByTopic(topicId: Long, now: Long = System.currentTimeMillis()): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsDueByTopic(topicId, now)

    suspend fun insertFlashcard(topicId: Long, front: String, back: String): Result<Long> = runCatching {
        val card = Flashcard(
            topicId = topicId,
            front = front.trim(),
            back = back.trim(),
            dueAt = System.currentTimeMillis() // Due immediately for initial learning
        )
        flashcardDao.insert(card)
    }

    suspend fun reviewFlashcard(card: Flashcard, grade: Int): Result<Flashcard> = runCatching {
        val sm2Result: Sm2ReviewResult = SpacedRepetition.calculateNextReview(
            currentEase = card.easeFactor,
            currentInterval = card.intervalDays,
            currentRepetitions = card.repetitions,
            grade = grade
        )
        val updatedCard = card.copy(
            easeFactor = sm2Result.easeFactor,
            intervalDays = sm2Result.intervalDays,
            repetitions = sm2Result.repetitions,
            dueAt = sm2Result.dueAt
        )
        flashcardDao.update(updatedCard)
        updatedCard
    }

    suspend fun deleteFlashcard(id: Long): Result<Unit> = runCatching {
        flashcardDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Quiz Questions
    // ----------------------------------------------------
    fun getQuestionsByTopic(topicId: Long): Flow<List<QuizQuestion>> = quizQuestionDao.getQuestionsByTopic(topicId)

    fun getQuestionsByTopics(topicIds: List<Long>): Flow<List<QuizQuestion>> =
        quizQuestionDao.getQuestionsByTopicIds(topicIds)

    suspend fun getQuestionsByTopicsList(topicIds: List<Long>): List<QuizQuestion> =
        quizQuestionDao.getQuestionsByTopicIdsList(topicIds)

    fun getWeakQuestions(): Flow<List<QuizQuestion>> = quizQuestionDao.getWeakQuestions()

    suspend fun getWeakQuestionsList(): List<QuizQuestion> = quizQuestionDao.getWeakQuestionsList()

    fun getWeakQuestionCount(): Flow<Int> = quizQuestionDao.getWeakQuestionCount()

    suspend fun insertQuizQuestion(
        topicId: Long,
        question: String,
        options: List<String>,
        correctIndex: Int
    ): Result<Long> = runCatching {
        val item = QuizQuestion(
            topicId = topicId,
            question = question.trim(),
            options = options.map { it.trim() },
            correctIndex = correctIndex
        )
        quizQuestionDao.insert(item)
    }

    suspend fun setQuestionWeakStatus(id: Long, isWeak: Boolean): Result<Unit> = runCatching {
        quizQuestionDao.updateWeakStatus(id, isWeak)
    }

    suspend fun deleteQuizQuestion(id: Long): Result<Unit> = runCatching {
        quizQuestionDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Study Sessions & Streaks & Stats
    // ----------------------------------------------------
    suspend fun recordStudySession(
        topicId: Long?,
        durationMinutes: Int,
        type: SessionType
    ): Result<Long> = runCatching {
        val session = StudySession(
            topicId = topicId,
            startedAt = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            type = type
        )
        studySessionDao.insert(session)
    }

    fun getStreakInfo(): Flow<StreakInfo> {
        return studySessionDao.getQualifyingSessionsForStreak().map { sessions ->
            StreakCalculator.calculate(sessions)
        }
    }

    fun getSubjectProgress(): Flow<List<SubjectProgress>> {
        return combine(
            subjectDao.getAllSubjects(),
            topicDao.getAllTopics()
        ) { subjects, topics ->
            val topicsBySubject = topics.groupBy { it.subjectId }
            subjects.map { subject ->
                val subjectTopics = topicsBySubject[subject.id] ?: emptyList()
                val total = subjectTopics.size
                val completed = subjectTopics.count { it.isCompleted }
                val percent = if (total > 0) completed.toFloat() / total else 0f
                SubjectProgress(
                    subject = subject,
                    completedTopics = completed,
                    totalTopics = total,
                    progressPercent = percent
                )
            }
        }
    }

    fun getWeeklyStudyMinutes(zoneId: ZoneId = ZoneId.systemDefault()): Flow<Map<LocalDate, Int>> {
        val today = LocalDate.now(zoneId)
        val sevenDaysAgo = today.minusDays(6)
        val startOfSevenDaysAgo = sevenDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()

        return studySessionDao.getSessionsSince(startOfSevenDaysAgo).map { sessions ->
            val map = (0..6).associate { offset ->
                sevenDaysAgo.plusDays(offset.toLong()) to 0
            }.toMutableMap()

            sessions.forEach { session ->
                val sessionDate = Instant.ofEpochMilli(session.startedAt).atZone(zoneId).toLocalDate()
                if (map.containsKey(sessionDate)) {
                    map[sessionDate] = (map[sessionDate] ?: 0) + session.durationMinutes
                }
            }
            map
        }
    }

    // ----------------------------------------------------
    // Today's Plan Generation (§7.3)
    // Priority order:
    // (1) flashcards due today (indicated via due count or plan prompt)
    // (2) topics scheduled for today in RevisionCalendar
    // (3) incomplete topics capped at 3 items shown on Home
    // ----------------------------------------------------
    fun getTodayPlanItems(limit: Int = 3, zoneId: ZoneId = ZoneId.systemDefault()): Flow<List<TodayPlanItem>> {
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return combine(
            subjectDao.getAllSubjects(),
            topicDao.getTopicsScheduledForDay(startOfDay, endOfDay),
            topicDao.getIncompleteTopics()
        ) { subjects, scheduledTopics, incompleteTopics ->
            val subjectMap = subjects.associateBy { it.id }
            val chosen = mutableListOf<Topic>()

            // 1. Scheduled for today
            for (topic in scheduledTopics) {
                if (chosen.none { it.id == topic.id }) {
                    chosen.add(topic)
                }
                if (chosen.size >= limit) break
            }

            // 2. If less than limit, fill with incomplete topics
            if (chosen.size < limit) {
                for (topic in incompleteTopics) {
                    if (chosen.none { it.id == topic.id }) {
                        chosen.add(topic)
                    }
                    if (chosen.size >= limit) break
                }
            }

            chosen.map { topic ->
                val subj = subjectMap[topic.subjectId]
                TodayPlanItem(
                    topic = topic,
                    subjectName = subj?.name ?: "Unknown Subject",
                    subjectColorHex = subj?.colorHex ?: "#7C9A82"
                )
            }
        }
    }

    suspend fun clearAllData() {
        database.clearAllTables()
    }
}
