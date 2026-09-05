package com.pathgeniusacademy.lifemate.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pathgeniusacademy.lifemate.model.TaskItem
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {
    fun schedule(context: Context, task: TaskItem) {
        val dueAt = task.dueAt ?: return
        if (!task.reminderEnabled || task.completed) return
        val delay = max(0L, dueAt - System.currentTimeMillis())
        val data = Data.Builder()
            .putString("taskId", task.id)
            .putString("title", task.title)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "lifemate_reminder_${task.id}",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("lifemate_reminder_$taskId")
    }
}
