package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.utils.ShiaAdhanPlayer

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.example.action.STOP_ADHAN") {
            ShiaAdhanPlayer.stop()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancelAll() // Cancel any sounding notifications
            return
        }

        if (action == "android.intent.action.BOOT_COMPLETED" || 
            action == "android.intent.action.MY_PACKAGE_REPLACED" ||
            action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = context.getSharedPreferences("shia_tracker_prefs", Context.MODE_PRIVATE)
            val isGlobalEnabled = prefs.getBoolean("adhan_enabled", false)
            if (isGlobalEnabled) {
                val lat = prefs.getFloat("city_lat", 0f).toDouble()
                val lon = prefs.getFloat("city_lon", 0f).toDouble()
                val tz = prefs.getFloat("city_tz", 0f).toDouble()
                if (lat != 0.0 && lon != 0.0) {
                    com.example.receiver.AdhanScheduler.scheduleAdhanAlarms(context, lat, lon, tz)
                }
            }
            return
        }

        val prefs = context.getSharedPreferences("shia_tracker_prefs", Context.MODE_PRIVATE)
        val isGlobalEnabled = prefs.getBoolean("adhan_enabled", false)
        if (!isGlobalEnabled) return

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Namaz"
        
        // Specific toggle check (default to true)
        val isPrayerEnabled = prefs.getBoolean("adhan_${prayerName.lowercase()}_enabled", true)
        if (!isPrayerEnabled) return

        val prefSound = prefs.getString("adhan_sound_key", "ali_fani") ?: "ali_fani"
        // 1. Play Shia Adhan audio Stream with offline Fallback
        ShiaAdhanPlayer.play(context, prefSound)

        // 2. Show prayer notification with a Mute/Stop button
        showAdhanNotification(context, prayerName)

        // 3. Reschedule all alarms for the future to keep the loop going
        val lat = prefs.getFloat("city_lat", 0f).toDouble()
        val lon = prefs.getFloat("city_lon", 0f).toDouble()
        val tz = prefs.getFloat("city_tz", 0f).toDouble()
        if (lat != 0.0 && lon != 0.0) {
            AdhanScheduler.scheduleAdhanAlarms(context, lat, lon, tz)
        }
    }

    private fun showAdhanNotification(context: Context, prayerName: String) {
        val channelId = "adhan_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Adhan Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan notifications for Shia Prayer Times"
                enableVibration(true)
                setBypassDnd(true)
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to stop/mute the Adhan
        val stopIntent = Intent(context, AdhanReceiver::class.java).apply {
            action = "com.example.action.STOP_ADHAN"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Adhan: Time for $prayerName")
            .setContentText("Observe your premium Shia $prayerName prayer now. Recite Namaz on its prime time!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_lock_silent_mode,
                "Stop Shia Adhan",
                stopPendingIntent
            )
            .build()

        manager.notify(prayerName.hashCode(), notification)
    }
}
