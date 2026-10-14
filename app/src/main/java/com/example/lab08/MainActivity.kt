package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lab08.data.TaskDatabase
import com.example.lab08.reminders.TaskReminderScheduler
import com.example.lab08.ui.TaskScreen
import com.example.lab08.ui.TaskViewModel
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TaskDatabase.getInstance(applicationContext)
        val taskViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(TaskViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(
                    applicationContext,
                    database.taskDao(),
                    TaskReminderScheduler(applicationContext)
                ) as T
            }
        })[TaskViewModel::class.java]

        setContent {
            Lab08Theme(dynamicColor = false) {
                TaskScreen(taskViewModel)
            }
        }
    }
}
