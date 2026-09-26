package com.studyoffline.app.data.local

import androidx.room.*
import com.studyoffline.app.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessionsList(): List<StudySession>

    @Query("SELECT * FROM study_sessions WHERE durationMinutes >= 5 ORDER BY startedAt ASC")
    fun getQualifyingSessionsForStreak(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE durationMinutes >= 5 ORDER BY startedAt ASC")
    suspend fun getQualifyingSessionsForStreakSync(): List<StudySession>

    @Query("SELECT * FROM study_sessions WHERE startedAt >= :sinceTimestamp ORDER BY startedAt ASC")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<StudySession>): List<Long>

    @Delete
    suspend fun delete(session: StudySession)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}
