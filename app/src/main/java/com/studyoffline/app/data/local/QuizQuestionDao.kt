package com.studyoffline.app.data.local

import androidx.room.*
import com.studyoffline.app.data.model.QuizQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizQuestionDao {
    @Query("SELECT * FROM quiz_questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions ORDER BY id ASC")
    suspend fun getAllQuestionsList(): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE topicId = :topicId ORDER BY id ASC")
    fun getQuestionsByTopic(topicId: Long): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE topicId = :topicId ORDER BY id ASC")
    suspend fun getQuestionsByTopicList(topicId: Long): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE topicId IN (:topicIds) ORDER BY id ASC")
    fun getQuestionsByTopicIds(topicIds: List<Long>): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE topicId IN (:topicIds) ORDER BY id ASC")
    suspend fun getQuestionsByTopicIdsList(topicIds: List<Long>): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE isWeak = 1 ORDER BY id ASC")
    fun getWeakQuestions(): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE isWeak = 1 ORDER BY id ASC")
    suspend fun getWeakQuestionsList(): List<QuizQuestion>

    @Query("SELECT COUNT(*) FROM quiz_questions WHERE isWeak = 1")
    fun getWeakQuestionCount(): Flow<Int>

    @Query("SELECT * FROM quiz_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): QuizQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuizQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestion>): List<Long>

    @Update
    suspend fun update(question: QuizQuestion)

    @Query("UPDATE quiz_questions SET isWeak = :isWeak WHERE id = :id")
    suspend fun updateWeakStatus(id: Long, isWeak: Boolean)

    @Delete
    suspend fun delete(question: QuizQuestion)

    @Query("DELETE FROM quiz_questions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM quiz_questions")
    suspend fun deleteAll()
}
