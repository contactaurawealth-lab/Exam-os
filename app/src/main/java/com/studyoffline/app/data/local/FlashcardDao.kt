package com.studyoffline.app.data.local

import androidx.room.*
import com.studyoffline.app.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    suspend fun getAllFlashcardsList(): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId ORDER BY id ASC")
    fun getFlashcardsByTopic(topicId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId ORDER BY id ASC")
    suspend fun getFlashcardsByTopicList(topicId: Long): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE dueAt <= :now ORDER BY dueAt ASC")
    fun getDueFlashcards(now: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE dueAt <= :now ORDER BY dueAt ASC")
    suspend fun getDueFlashcardsList(now: Long): List<Flashcard>

    @Query("SELECT COUNT(*) FROM flashcards WHERE dueAt <= :now")
    fun getDueFlashcardCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE dueAt <= :now")
    suspend fun getDueFlashcardCountSync(now: Long): Int

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId AND dueAt <= :now ORDER BY dueAt ASC")
    fun getFlashcardsDueByTopic(topicId: Long, now: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId AND dueAt <= :now ORDER BY dueAt ASC")
    suspend fun getFlashcardsDueByTopicList(topicId: Long, now: Long): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getFlashcardById(id: Long): Flashcard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Flashcard>): List<Long>

    @Update
    suspend fun update(card: Flashcard)

    @Delete
    suspend fun delete(card: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM flashcards")
    suspend fun deleteAll()
}
