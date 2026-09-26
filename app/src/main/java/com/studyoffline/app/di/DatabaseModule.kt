package com.studyoffline.app.di

import android.content.Context
import androidx.room.Room
import com.studyoffline.app.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "study_offline.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao = database.subjectDao()

    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao = database.topicDao()

    @Provides
    fun provideFlashcardDao(database: AppDatabase): FlashcardDao = database.flashcardDao()

    @Provides
    fun provideQuizQuestionDao(database: AppDatabase): QuizQuestionDao = database.quizQuestionDao()

    @Provides
    fun provideStudySessionDao(database: AppDatabase): StudySessionDao = database.studySessionDao()
}
