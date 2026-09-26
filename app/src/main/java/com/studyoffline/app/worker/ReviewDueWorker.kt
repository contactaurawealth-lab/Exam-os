package com.studyoffline.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.data.repository.StudyRepository
import com.studyoffline.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ReviewDueWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = preferencesRepository.userSettingsFlow.first()
            if (settings.masterNotificationEnabled && settings.reviewDueEnabled) {
                val dueCount = studyRepository.getDueFlashcardCountSync()
                if (dueCount > 0) {
                    NotificationHelper.showReviewDueNotification(context, dueCount)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
