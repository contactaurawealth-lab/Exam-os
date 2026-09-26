package com.studyoffline.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.studyoffline.app.data.local.AppDatabase
import com.studyoffline.app.data.model.*
import com.studyoffline.app.data.preferences.UserSettings
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class BackupRoot(
    val schemaVersion: Int = 1,
    val exportedAt: String,
    val appVersion: String = "1.0.0",
    val data: BackupData
)

data class BackupData(
    val subjects: List<Subject> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val settings: BackupSettings
)

data class BackupSettings(
    val themeMode: String,
    val accentColorHex: String,
    val examGoalDate: Long?,
    val examName: String?,
    val dailyStudyMinutes: Int
)

sealed class ImportValidationResult {
    data class Success(val backupRoot: BackupRoot, val warnings: List<String>) : ImportValidationResult()
    data class Error(val message: String) : ImportValidationResult()
}

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val subjects = database.subjectDao().getAllSubjectsList()
        val topics = database.topicDao().getAllTopicsList()
        val flashcards = database.flashcardDao().getAllFlashcardsList()
        val questions = database.quizQuestionDao().getAllQuestionsList()
        val sessions = database.studySessionDao().getAllSessionsList()
        val settings = preferencesRepository.userSettingsFlow.first()

        val backupData = BackupData(
            subjects = subjects,
            topics = topics,
            flashcards = flashcards,
            quizQuestions = questions,
            studySessions = sessions,
            settings = BackupSettings(
                themeMode = settings.themeMode,
                accentColorHex = settings.accentColorHex,
                examGoalDate = settings.examGoalDate,
                examName = settings.examName,
                dailyStudyMinutes = settings.dailyStudyMinutes
            )
        )

        val root = BackupRoot(
            schemaVersion = 1,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            appVersion = "1.0.0",
            data = backupData
        )

        gson.toJson(root)
    }

    suspend fun writeBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = generateBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            } ?: error("Failed to open output stream")
        }
    }

    suspend fun validateBackup(uri: Uri): ImportValidationResult = withContext(Dispatchers.IO) {
        try {
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            } ?: return@withContext ImportValidationResult.Error("Could not read backup file.")

            val jsonElement = try {
                JsonParser.parseString(jsonContent)
            } catch (e: Exception) {
                return@withContext ImportValidationResult.Error("This file does not contain valid JSON.")
            }

            if (!jsonElement.isJsonObject) {
                return@withContext ImportValidationResult.Error("This file doesn't look like a valid StudyOffline backup.")
            }

            val jsonObject = jsonElement.asJsonObject

            // 1. Check schemaVersion
            if (!jsonObject.has("schemaVersion")) {
                return@withContext ImportValidationResult.Error("This file doesn't look like a valid StudyOffline backup.")
            }
            val schemaVersion = jsonObject.get("schemaVersion").asInt
            if (schemaVersion > 1) {
                return@withContext ImportValidationResult.Error(
                    "This backup was made with a newer app version (schema $schemaVersion). Update the app to import it."
                )
            }

            // 2. Check data section
            if (!jsonObject.has("data") || !jsonObject.get("data").isJsonObject) {
                return@withContext ImportValidationResult.Error("Missing 'data' section in backup.")
            }

            val backupRoot = try {
                gson.fromJson(jsonObject, BackupRoot::class.java)
            } catch (e: Exception) {
                return@withContext ImportValidationResult.Error("Malformed backup structure: ${e.message}")
            }

            // 3. Foreign Key Integrity Checks
            val warnings = mutableListOf<String>()
            val subjectIds = backupRoot.data.subjects.map { it.id }.toSet()
            val validTopics = mutableListOf<Topic>()

            for (topic in backupRoot.data.topics) {
                if (subjectIds.contains(topic.subjectId)) {
                    validTopics.add(topic)
                } else {
                    warnings.add("Topic '${topic.name}' references non-existent subject ID ${topic.subjectId}.")
                }
            }

            val validTopicIds = validTopics.map { it.id }.toSet()
            val validFlashcards = mutableListOf<Flashcard>()
            for (card in backupRoot.data.flashcards) {
                if (validTopicIds.contains(card.topicId)) {
                    validFlashcards.add(card)
                } else {
                    warnings.add("Flashcard '${card.front}' references missing topic ID ${card.topicId}.")
                }
            }

            val validQuestions = mutableListOf<QuizQuestion>()
            for (q in backupRoot.data.quizQuestions) {
                if (validTopicIds.contains(q.topicId)) {
                    validQuestions.add(q)
                } else {
                    warnings.add("Question '${q.question}' references missing topic ID ${q.topicId}.")
                }
            }

            val sanitizedRoot = backupRoot.copy(
                data = backupRoot.data.copy(
                    topics = validTopics,
                    flashcards = validFlashcards,
                    quizQuestions = validQuestions
                )
            )

            ImportValidationResult.Success(sanitizedRoot, warnings)
        } catch (e: Exception) {
            ImportValidationResult.Error("Validation failed: ${e.message}")
        }
    }

    /**
     * Executes import inside a Room @Transaction for full rollback on any failure (PRD §10)
     */
    suspend fun executeImport(
        backupRoot: BackupRoot,
        isReplace: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                if (isReplace) {
                    database.clearAllTables()
                }

                if (isReplace) {
                    // Straight replace with original IDs
                    database.subjectDao().insertAll(backupRoot.data.subjects)
                    database.topicDao().insertAll(backupRoot.data.topics)
                    database.flashcardDao().insertAll(backupRoot.data.flashcards)
                    database.quizQuestionDao().insertAll(backupRoot.data.quizQuestions)
                    database.studySessionDao().insertAll(backupRoot.data.studySessions)
                } else {
                    // Merge: re-map foreign keys to prevent ID collisions
                    val subjectIdMap = mutableMapOf<Long, Long>()
                    for (subject in backupRoot.data.subjects) {
                        val newId = database.subjectDao().insert(subject.copy(id = 0))
                        subjectIdMap[subject.id] = newId
                    }

                    val topicIdMap = mutableMapOf<Long, Long>()
                    for (topic in backupRoot.data.topics) {
                        val mappedSubjId = subjectIdMap[topic.subjectId] ?: continue
                        val newId = database.topicDao().insert(topic.copy(id = 0, subjectId = mappedSubjId))
                        topicIdMap[topic.id] = newId
                    }

                    for (card in backupRoot.data.flashcards) {
                        val mappedTopicId = topicIdMap[card.topicId] ?: continue
                        database.flashcardDao().insert(card.copy(id = 0, topicId = mappedTopicId))
                    }

                    for (q in backupRoot.data.quizQuestions) {
                        val mappedTopicId = topicIdMap[q.topicId] ?: continue
                        database.quizQuestionDao().insert(q.copy(id = 0, topicId = mappedTopicId))
                    }

                    for (session in backupRoot.data.studySessions) {
                        val mappedTopicId = if (session.topicId != null) topicIdMap[session.topicId] else null
                        database.studySessionDao().insert(session.copy(id = 0, topicId = mappedTopicId))
                    }
                }
            }

            // Restore settings if present
            val settings = backupRoot.data.settings
            preferencesRepository.setThemeMode(settings.themeMode)
            preferencesRepository.setAccentColor(settings.accentColorHex)
            if (settings.examGoalDate != null) {
                preferencesRepository.setExamGoal(settings.examName ?: "", settings.examGoalDate)
            }
            preferencesRepository.setDailyStudyMinutes(settings.dailyStudyMinutes)
        }
    }

    suspend fun resetApp(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.clearAllTables()
            preferencesRepository.clear()
        }
    }
}
