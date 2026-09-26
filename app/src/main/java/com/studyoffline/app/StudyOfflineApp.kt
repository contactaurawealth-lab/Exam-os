package com.studyoffline.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.studyoffline.app.notification.NotificationHelper
import com.studyoffline.app.worker.ReviewDueWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class StudyOfflineApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        schedulePeriodicReviewCheck()
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
