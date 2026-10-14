package com.example.lab08.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lab08.MainActivity
import com.example.lab08.R
import com.example.lab08.data.TaskDatabase

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getInt(TASK_ID, -1)
        if (taskId < 0) return Result.failure()
        val task = TaskDatabase.getInstance(applicationContext).taskDao().getTaskById(taskId)
            ?: return Result.success()
        if (task.isCompleted || task.isDeleted || task.reminderAt == null) return Result.success()

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        createChannel()
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task_notification)
            .setContentTitle("Tarea pendiente")
            .setContentText(task.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(taskId, notification)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Recordatorios de tareas", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos de tareas pendientes" }
            applicationContext.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val TASK_ID = "task_id"
        private const val CHANNEL_ID = "task_reminders"
    }
}
