package com.example.halalyticscompose

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.example.halalyticscompose.data.notification.NotificationWorker
import com.example.halalyticscompose.data.worker.MedicineReminderScheduler
import com.example.halalyticscompose.utils.CrashReporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HalalyticsApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() {
            val builder = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)

            // Prevent rare startup crash if WorkManager asks config before Hilt field is initialized.
            if (this::workerFactory.isInitialized) {
                builder.setWorkerFactory(workerFactory)
            } else {
                Log.w("HalalyticsApp", "HiltWorkerFactory not initialized yet; using default worker factory")
            }
            return builder.build()
        }
    
    override fun onCreate() {
        super.onCreate()

        // Capture fatal crashes into local storage for easier diagnosis from app side.
        CrashReporter.install(this)
        
        try {
            com.facebook.FacebookSdk.sdkInitialize(applicationContext)
        } catch (e: Exception) {
            Log.e("HalalyticsApp", "Failed to initialize Facebook SDK: ${e.message}", e)
        }
        
        // Delay scheduling to ensure WorkManager uses our custom configuration
        // WorkManager.getInstance(this) will now use workManagerConfiguration
        try {
            // Schedule offline data synchronization
            com.example.halalyticscompose.data.worker.SyncManager.scheduleSync(this)
        } catch (e: Exception) {
            Log.e("HalalyticsApp", "Error scheduling sync worker: ${e.message}", e)
        }

        try {
            // Schedule periodic notification worker
            NotificationWorker.schedulePeriodicWork(this)
        } catch (e: Exception) {
            Log.e("HalalyticsApp", "Error scheduling notification worker: ${e.message}", e)
        }

        try {
            // Schedule medicine reminder worker
            MedicineReminderScheduler().scheduleReminders(this)
        } catch (e: Exception) {
            Log.e("HalalyticsApp", "Error scheduling medicine reminder worker: ${e.message}", e)
        }
    }
}
