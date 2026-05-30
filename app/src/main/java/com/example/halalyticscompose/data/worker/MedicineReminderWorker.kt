package com.example.halalyticscompose.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.halalyticscompose.MainActivity
import com.example.halalyticscompose.R
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import kotlinx.coroutines.flow.first

class MedicineReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

        // Keep only doWork
    override suspend fun doWork(): Result {
        return try {
            val sessionManager = SessionManager(applicationContext)
            val apiService = com.example.halalyticscompose.data.network.ApiConfig.apiService
            
            val userId = sessionManager.getUserId()
            if (userId == 0) return Result.failure()
            val token = sessionManager.getBearerToken() ?: sessionManager.getAuthToken()?.let { "Bearer $it" } ?: return Result.failure()
            
            // Get user reminders
            val response = apiService.getUserMedicineReminders(token)
            
            if (response.isSuccessful) {
                val reminders: List<MedicationReminderItem> = response.body()?.data ?: emptyList()
                
                // Clear old alarms before recreating
                for (reminder in reminders) {
                    MedicineAlarmHelper.cancelAlarm(applicationContext, reminder.id)
                }

                val now = java.util.Calendar.getInstance()
                
                // Schedule notifications using Exact Alarm dynamically
                for (reminder in reminders) {
                    if (!reminder.isActive) continue
                    
                    val times = reminder.scheduleTimes ?: reminder.timeSlots ?: emptyList()
                    if (times.isEmpty()) continue

                    times.forEachIndexed { index, timeStr ->
                        try {
                            val parts = timeStr.split(":")
                            if (parts.size == 2) {
                                val alarmCalendar = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
                                    set(java.util.Calendar.MINUTE, parts[1].toInt())
                                    set(java.util.Calendar.SECOND, 0)
                                    set(java.util.Calendar.MILLISECOND, 0)
                                }
                                
                                // If time already passed today, schedule for tomorrow
                                if (alarmCalendar.before(now)) {
                                    alarmCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }
                                
                                // Make a unique ID per time slot
                                val uniqueAlarmId = (reminder.id * 100) + index
                                val mName = reminder.drug?.name?.takeIf { it.isNotBlank() } ?: reminder.medicineName.takeIf { it.isNotBlank() } ?: "Obat Anda"
                                
                                MedicineAlarmHelper.scheduleExactAlarm(
                                    applicationContext,
                                    uniqueAlarmId,
                                    mName,
                                    alarmCalendar.timeInMillis
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
