package com.v2ray.app.subscription

import android.content.Context
import android.util.Log
import androidx.work.*
import com.v2ray.app.data.AppDatabase
import com.v2ray.app.data.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

object SubscriptionUpdater {
    private const val TAG = "SubscriptionUpdater"
    private const val WORK_NAME = "subscription_update"

    private lateinit var context: Context
    private val db: AppDatabase by lazy { AppDatabase.getInstance(context) }

    fun init(context: Context) {
        this.context = context.applicationContext
        scheduleAll()
    }

    fun scheduleAll() {
        val subscriptions = runBlocking {
            db.subscriptionDao().getActiveAutoUpdate().collect { list ->
                return@collect list
            }
        }

        if (subscriptions.isEmpty()) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }

        // محاسبه‌ی کوچکترین بازه
        var minInterval = subscriptions.minOfOrNull { it.updateInterval } ?: 60
        if (minInterval < 15) minInterval = 15

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SubscriptionWorker>(
            minInterval.toLong(), TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(TAG, "Scheduled subscription update every $minInterval minutes")
    }

    class SubscriptionWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {

        override suspend fun doWork(): Result {
            return try {
                val db = AppDatabase.getInstance(applicationContext)
                val subscriptions = db.subscriptionDao().getActiveAutoUpdate().collect { list ->
                    return@collect list
                }

                for (sub in subscriptions) {
                    if (!sub.needsUpdate()) continue

                    try {
                        val profiles = SubscriptionParser.fetchAndParse(sub.url)
                        if (profiles.isNotEmpty()) {
                            // ذخیره‌سازی پروفایل‌ها (می‌توانید به دیتابیس پروفایل اضافه کنید)
                            Log.d(TAG, "Updated ${profiles.size} profiles from ${sub.name}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update ${sub.name}", e)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Subscription update failed", e)
                Result.retry()
            }
        }
    }
}
