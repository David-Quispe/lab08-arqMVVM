package com.example.lab08.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.lab08.data.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val cloudStatus by viewModel.cloudStatus.collectAsState()
    val cloudSignedIn by viewModel.cloudSignedIn.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    var newTask by rememberSaveable { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Task?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    var pendingReminder by remember { mutableStateOf<Task?>(null) }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val task = pendingReminder
        pendingReminder = null
        if (granted && task != null) {
            showReminderPicker(context) { viewModel.setReminder(task, it) }
        } else if (!granted) {
            Toast.makeText(context, "Activa las notificaciones para recibir avisos", Toast.LENGTH_LONG).show()
        }
    }
    val visibleTasks = remember(tasks, search) {
        tasks.filter { it.description.contains(search.trim(), ignoreCase = true) }
    }
    val pendingCount = tasks.count { !it.isCompleted }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        label = { Text("Nueva tarea") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.addTask(newTask)
                            newTask = ""
                        },
                        enabled = newTask.isNotBlank(),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Agregar tarea",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text("HOY", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Mis tareas", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold)
            Text(
                "$pendingCount ${if (pendingCount == 1) "pendiente" else "pendientes"} · " +
                    SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-PE")).format(Date()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    cloudStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    maxLines = 2
                )
                TextButton(onClick = {
                    if (cloudSignedIn) viewModel.syncNow()
                    else viewModel.signInWithGoogle(context)
                }) { Text(if (cloudSignedIn) "Sincronizar" else "Conectar") }
                if (cloudSignedIn) {
                    TextButton(onClick = { viewModel.signOut(context) }) { Text("Salir") }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar tareas") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Lista de tareas", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (tasks.isNotEmpty()) {
                    TextButton(onClick = { confirmDeleteAll = true }) { Text("Eliminar todas") }
                }
            }
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            if (visibleTasks.isEmpty()) {
                Text(
                    if (search.isBlank()) "Aún no hay tareas. Agrega una abajo."
                    else "No encontramos tareas con ese texto.",
                    modifier = Modifier.padding(vertical = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { deleteTarget = task },
                            onReminder = {
                                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    pendingReminder = task
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    showReminderPicker(context) { viewModel.setReminder(task, it) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar tarea") },
            text = { Text("¿Eliminar «${task.description}»?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    deleteTarget = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") } }
        )
    }
    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Eliminar todas las tareas") },
            text = { Text("Se quitarán todas las tareas de la lista.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllTasks()
                    confirmDeleteAll = false
                }) { Text("Eliminar todas") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onReminder: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Marcar pendiente" else "Marcar completada",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                task.reminderAt?.let {
                    Text(
                        "Aviso: " + SimpleDateFormat("d MMM, HH:mm", Locale.forLanguageTag("es-PE")).format(Date(it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onReminder, enabled = !task.isCompleted) {
                Icon(Icons.Outlined.NotificationsNone, contentDescription = "Programar recordatorio",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar tarea",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun showReminderPicker(context: Context, onSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
    DatePickerDialog(context, { _, year, month, day ->
        TimePickerDialog(context, { _, hour, minute ->
            val selected = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            if (selected <= System.currentTimeMillis()) {
                Toast.makeText(context, "Elige una fecha y hora futura", Toast.LENGTH_LONG).show()
            } else {
                onSelected(selected)
                Toast.makeText(context, "Recordatorio programado", Toast.LENGTH_SHORT).show()
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}
