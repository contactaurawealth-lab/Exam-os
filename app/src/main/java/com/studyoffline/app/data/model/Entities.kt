package com.studyoffline.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("scheduledDate")]
)
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val notes: String? = null,
    val isCompleted: Boolean = false,
    val scheduledDate: Long? = null, // UTC/Local midnight timestamp
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId"), Index("dueAt")]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val front: String,
    val back: String,
    // SM-2 spaced repetition state
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val isWeak: Boolean = false
)

enum class SessionType {
    POMODORO, QUIZ, FLASHCARD, FREE
}

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("topicId"), Index("startedAt")]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val type: SessionType
)
