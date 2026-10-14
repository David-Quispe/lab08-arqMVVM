package com.example.lab08.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab08.cloud.CloudSyncManager
import com.example.lab08.data.Task
import com.example.lab08.data.TaskDao
import com.example.lab08.reminders.TaskReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    context: Context,
    private val dao: TaskDao,
    private val reminders: TaskReminderScheduler
) : ViewModel() {
    private val cloud = CloudSyncManager(context, dao, reminders, viewModelScope)
    val cloudStatus: StateFlow<String> = cloud.status
    val cloudSignedIn: StateFlow<Boolean> = cloud.signedIn
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            dao.observeTasks().collect { _tasks.value = it }
        }
    }

    fun addTask(description: String) {
        val cleaned = description.trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            dao.insertTask(Task(description = cleaned))
            cloud.requestSync()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val current = dao.getTaskById(task.id) ?: return@launch
            val updated = current.copy(
                isCompleted = !current.isCompleted,
                updatedAt = maxOf(System.currentTimeMillis(), current.updatedAt + 1)
            )
            dao.updateTask(updated)
            reminders.update(updated)
            cloud.requestSync()
        }
    }

    fun setReminder(task: Task, reminderAt: Long?) {
        viewModelScope.launch {
            val current = dao.getTaskById(task.id) ?: return@launch
            val updated = current.copy(
                reminderAt = reminderAt,
                updatedAt = maxOf(System.currentTimeMillis(), current.updatedAt + 1)
            )
            dao.updateTask(updated)
            reminders.update(updated)
            cloud.requestSync()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val current = dao.getTaskById(task.id) ?: return@launch
            dao.updateTask(current.copy(
                isDeleted = true,
                reminderAt = null,
                updatedAt = maxOf(System.currentTimeMillis(), current.updatedAt + 1)
            ))
            reminders.cancel(task.id)
            cloud.requestSync()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.getAllTasks().forEach { reminders.cancel(it.id) }
            dao.deleteAllTasks(System.currentTimeMillis())
            cloud.requestSync()
        }
    }

    fun signInWithGoogle(context: Context) {
        cloud.signInWithGoogle(context)
    }

    fun signOut(context: Context) {
        cloud.signOut(context)
    }

    fun syncNow() {
        cloud.requestSync()
    }

    override fun onCleared() {
        cloud.close()
        super.onCleared()
    }
}
