package com.example.lab08.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.lab08.data.Task
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun update(task: Task) {
        val reminderAt = task.reminderAt
        if (task.isCompleted || task.isDeleted || reminderAt == null || reminderAt <= System.currentTimeMillis()) {
            cancel(task.id)
            return
        }
        val delay = (reminderAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(TaskReminderWorker.TASK_ID to task.id))
            .build()
        workManager.enqueueUniqueWork(name(task.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: Int) {
        workManager.cancelUniqueWork(name(taskId))
    }

    private fun name(taskId: Int) = "task-reminder-$taskId"
}
