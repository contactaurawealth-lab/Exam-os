package com.studyoffline.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studyoffline.app.data.model.Flashcard
import com.studyoffline.app.data.model.QuizQuestion
import com.studyoffline.app.data.model.StudySession
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.data.model.Topic

@Database(
    entities = [
        Subject::class,
        Topic::class,
        Flashcard::class,
        QuizQuestion::class,
        StudySession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun studySessionDao(): StudySessionDao
}
