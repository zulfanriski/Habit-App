package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.habitapp.utils.NOTIF_UNIQUE_WORK

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = getParcelableExtra(intent, HABIT, Habit::class.java)

        if (habit != null){
            findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

            val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

            //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
            viewModel.setInitialTime(habit.minutesFocus)
            viewModel.currentTimeString.observe(this){
                findViewById<TextView>(R.id.tv_count_down).text = it
            }

            //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
            val channel = getString(R.string.notify_channel_name)
            val wM = WorkManager.getInstance(this)

            val data = Data.Builder().putInt(HABIT_ID, habit.id)
                .putString(HABIT_TITLE, habit.title)
                .putString(NOTIFICATION_CHANNEL_ID, channel)
                .build()

            val req = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(data)
                .addTag(NOTIF_UNIQUE_WORK)
                .build()

            viewModel.eventCountDownFinish.observe(this) { eventCountDownFinish ->
                eventCountDownFinish?.let { shouldEnqueue ->
                    if (shouldEnqueue) {
                        enqueueWorkWithUniqueName(wM, NOTIF_UNIQUE_WORK, req)
                        updateButtonState(false)
                    }
                }
            }


            findViewById<Button>(R.id.btn_start).setOnClickListener {
                updateButtonState(true)
                viewModel.startTimer()
            }

            findViewById<Button>(R.id.btn_stop).setOnClickListener {
                viewModel.resetTimer()
                updateButtonState(false)
                wM.cancelUniqueWork(NOTIF_UNIQUE_WORK)
            }
        }

    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }

    private fun enqueueWorkWithUniqueName(
        workManager: WorkManager,
        uniqueWorkName: String,
        workRequest: OneTimeWorkRequest
    ) {
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}