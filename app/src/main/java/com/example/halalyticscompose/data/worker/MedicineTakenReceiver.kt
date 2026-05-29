package com.example.halalyticscompose.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.halalyticscompose.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.halalyticscompose.MainActivity
class MedicineTakenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MEDICINE_TAKEN") {
            val reminderId = intent.getIntExtra("reminder_id", -1)
            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUserId()
            val token = sessionManager.getBearerToken().orEmpty()
            
            if (reminderId != -1 && token.isNotBlank()) {
                // Mark medicine as taken in background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val apiService = com.example.halalyticscompose.data.network.ApiConfig.apiService
                        val response = apiService.markMedicineAsTaken(token, reminderId)
                        if (response.isSuccessful) {
                            Log.d("MedicineTaken", "Successfully marked medicine as taken")
                        } else {
                            Log.e("MedicineTaken", "Failed to mark medicine as taken")
                        }
                    } catch (e: Exception) {
                        Log.e("MedicineTaken", "Error marking medicine as taken", e)
                    }
                }
                
                // Show confirmation notification
                showTakenConfirmation(context, reminderId)
            }
        }
        else if (intent.action == "MEDICINE_REMINDER_ALARM") {
            val reminderId = intent.getIntExtra("REMINDER_ID", 0)
            val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Obat"
            triggerLoudAlarmNotification(context, reminderId, medicineName)
        }
    }

    private fun triggerLoudAlarmNotification(context: Context, reminderId: Int, medicineName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medicine_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Health Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loud alarm for medicine reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "medicine_reminders")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action to mark as taken immediately
        val takenIntent = Intent(context, MedicineTakenReceiver::class.java).apply {
            action = "MEDICINE_TAKEN"
            putExtra("reminder_id", reminderId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId * 10,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Waktu Minum Obat!")
            .setContentText("Waktunya minum $medicineName sekarang.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Sudah Diminum", takenPendingIntent)
            // Use full-screen intent so it pops up aggressively
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(reminderId, notification)
    }

    private fun showTakenConfirmation(context: Context, reminderId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "medicine_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Taken Confirmation",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Obat Telah Diminum")
            .setContentText("Hebat! Obatmu sudah tercatat.")
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(reminderId + 10000, notification)
    }
}
