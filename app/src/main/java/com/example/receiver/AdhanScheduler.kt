package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.utils.PrayerTimeCalculator
import java.util.*

object AdhanScheduler {
    fun scheduleAdhanAlarms(context: Context, lat: Double, lon: Double, tzOffset: Double) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val times = PrayerTimeCalculator.calculateTimes(year, month, day, lat, lon, tzOffset)
        val prayers = listOf(
            "Fajr" to times.fajrString,
            "Dhuhr" to times.dhuhrString,
            "Maghrib" to times.maghribString
        )
        
        prayers.forEach { (name, timeStr) ->
            try {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    
                    val triggerCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    
                    val intent = Intent(context, AdhanReceiver::class.java).apply {
                        putExtra("PRAYER_NAME", name)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        name.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerCal.timeInMillis,
                                pendingIntent
                            )
                        } catch (e: Exception) {
                            try {
                                alarmManager.set(
                                    AlarmManager.RTC_WAKEUP,
                                    triggerCal.timeInMillis,
                                    pendingIntent
                                )
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    } else {
                        try {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                triggerCal.timeInMillis,
                                pendingIntent
                            )
                        } catch (e: Exception) {
                            try {
                                alarmManager.set(
                                    AlarmManager.RTC_WAKEUP,
                                    triggerCal.timeInMillis,
                                    pendingIntent
                                )
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun cancelAllAdhanAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        prayers.forEach { name ->
            val intent = Intent(context, AdhanReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                name.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
