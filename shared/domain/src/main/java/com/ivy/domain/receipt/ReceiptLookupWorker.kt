package com.ivy.domain.receipt

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Retries pending receipt lookups when online, backing off from about 15 minutes. */
@HiltWorker
class ReceiptLookupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val checkPendingReceipts: CheckPendingReceiptsUseCase,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = if (checkPendingReceipts.check()) {
        Result.retry()
    } else {
        Result.success()
    }
}

class ReceiptLookupScheduler @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    companion object {
        private const val UNIQUE_WORK_NAME = "receipt_lookup"
        private const val BACKOFF_MINUTES = 15L
    }

    fun schedule() {
        val request = OneTimeWorkRequestBuilder<ReceiptLookupWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_MINUTES, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
