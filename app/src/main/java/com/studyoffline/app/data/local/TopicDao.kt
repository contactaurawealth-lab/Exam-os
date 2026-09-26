package com.studyoffline.app.data.local

import androidx.room.*
import com.studyoffline.app.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY createdAt ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics ORDER BY createdAt ASC")
    suspend fun getAllTopicsList(): List<Topic>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY createdAt ASC")
    fun getTopicsBySubject(subjectId: Long): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY createdAt ASC")
    suspend fun getTopicsBySubjectList(subjectId: Long): List<Topic>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    fun getTopicById(id: Long): Flow<Topic?>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicByIdSync(id: Long): Topic?

    @Query("SELECT * FROM topics WHERE scheduledDate >= :startOfDay AND scheduledDate < :endOfDay ORDER BY isCompleted ASC, createdAt ASC")
    fun getTopicsScheduledForDay(startOfDay: Long, endOfDay: Long): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE scheduledDate >= :startOfDay AND scheduledDate < :endOfDay ORDER BY isCompleted ASC, createdAt ASC")
    suspend fun getTopicsScheduledForDaySync(startOfDay: Long, endOfDay: Long): List<Topic>

    @Query("SELECT * FROM topics WHERE scheduledDate IS NOT NULL ORDER BY scheduledDate ASC")
    fun getAllScheduledTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE isCompleted = 0 ORDER BY createdAt ASC")
    fun getIncompleteTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE isCompleted = 0 ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getIncompleteTopicsSync(limit: Int): List<Topic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: Topic): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<Topic>): List<Long>

    @Update
    suspend fun update(topic: Topic)

    @Query("UPDATE topics SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletion(id: Long, isCompleted: Boolean)

    @Query("UPDATE topics SET scheduledDate = :scheduledDate WHERE id = :id")
    suspend fun updateScheduledDate(id: Long, scheduledDate: Long?)

    @Query("UPDATE topics SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    @Delete
    suspend fun delete(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM topics")
    suspend fun deleteAll()
}
