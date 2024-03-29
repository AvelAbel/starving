package com.example.starving

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.math.max
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var increaseTimerButton: Button
    private lateinit var starvingTextView: TextView
    private var timerValue: Long = 0
    private val timer = Timer()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val CHANNEL_ID = "timer_channel_id"
        const val NOTIFICATION_ID = 1
        const val TIMER_VALUE_KEY = "timer_value"
        const val TIMER_TIMESTAMP_KEY = "timer_timestamp"  // Добавлено объявление константы
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.timerTextView)
        increaseTimerButton = findViewById(R.id.increaseTimerButton)
        starvingTextView = findViewById(R.id.starvingTextView)

        sharedPreferences = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

        // Убедитесь, что restoreTimerState вызывается после инициализации всех View
        restoreTimerState()

        increaseTimerButton.setOnClickListener {
            val intent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_INCREASE_TIMER
            }
            startService(intent)
        }

        createNotificationChannel()
        startTimer()

        registerReceiver(timerUpdateReceiver, IntentFilter(TimerService.ACTION_TIMER_UPDATED))

    }


    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            restoreTimerState() // Обновляем значение таймера
        }
    }

    private fun saveTimerState() {
        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit()
            .putLong(TIMER_VALUE_KEY, timerValue)
            .putLong(TIMER_TIMESTAMP_KEY, currentTime)
            .apply()
    }

    private fun startTimer() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (timerValue > 0) {
                    timerValue -= 1000
                    runOnUiThread {
                        updateTimerDisplay()
                        updateNotification()
                    }
                } else {
                    runOnUiThread {
                        showStarvingMessage()
                        updateNotification()
                    }
                }
            }
        }, 0, 1000)
    }

    private fun updateTimerDisplay() {
        val hours = timerValue / 3600000
        val minutes = (timerValue % 3600000) / 60000
        val seconds = (timerValue % 60000) / 1000
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun showStarvingMessage() {
        starvingTextView.apply {
            text = "STARVING"
            setTextColor(resources.getColor(R.color.red))
            visibility = TextView.VISIBLE
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        updateNotification() // Создайте уведомление при запуске
    }

    private fun updateNotification() {
        val notificationIcon = if (timerValue > 0) R.drawable.ic_notification else R.drawable.ic_notification_starving
        val notificationText = "Time left: ${formatTime(timerValue)}"
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle("Timer Running")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun saveTimerValue() {
        sharedPreferences.edit().putLong(TIMER_VALUE_KEY, timerValue).apply()
    }

    private fun restoreTimerState() {
        val savedTime = sharedPreferences.getLong(TIMER_TIMESTAMP_KEY, 0)
        val savedTimerValue = sharedPreferences.getLong(TIMER_VALUE_KEY, 0)

        timerValue = sharedPreferences.getLong(TIMER_VALUE_KEY, 5000)


        // Обновляем отображение таймера
        updateTimerDisplay()
    }

    private fun formatTime(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        unregisterReceiver(timerUpdateReceiver)
        saveTimerState()
        timer.cancel()
        super.onDestroy()
    }
}
