package com.studyoffline.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.domain.blocker.AppBlockerManager
import com.studyoffline.app.notification.NotificationHelper
import com.studyoffline.app.worker.ReviewDueWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class StudyOfflineApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        schedulePeriodicReviewCheck()
        observeAppBlockerSettings()
    }

    private fun observeAppBlockerSettings() {
        applicationScope.launch {
            preferencesRepository.userSettingsFlow.collectLatest { settings ->
                AppBlockerManager.syncFromSettings(settings)
            }
        }
    }

    private fun schedulePeriodicReviewCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ReviewDueWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ReviewDueWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
