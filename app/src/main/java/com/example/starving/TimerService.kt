package com.example.starving

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.*

class TimerService : Service() {

    private var timerValue: Long = 5000 // Начальное значение таймера
    private val timer = Timer()
    private val sharedPreferences by lazy { getSharedPreferences("timer_prefs", MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        restoreTimerState()
        startTimer()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_INCREASE_TIMER -> increaseTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        // Логика таймера и уведомлений
    }

    private fun increaseTimer() {
        timerValue += 5000 // Увеличиваем на 24 часа
        saveTimerState()
        sendBroadcast(Intent(ACTION_TIMER_UPDATED))
    }


    private fun saveTimerState() {
        sharedPreferences.edit().putLong(MainActivity.TIMER_VALUE_KEY, timerValue).apply()
    }

    private fun restoreTimerState() {
        timerValue = sharedPreferences.getLong(MainActivity.TIMER_VALUE_KEY, 5000)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_INCREASE_TIMER = "action_increase_timer"
        const val ACTION_TIMER_UPDATED = "action_timer_updated"
    }
}
