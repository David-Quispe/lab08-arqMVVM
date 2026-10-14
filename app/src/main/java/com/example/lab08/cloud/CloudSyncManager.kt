package com.example.lab08.cloud

import android.content.Context
import com.example.lab08.R
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.lab08.data.Task
import com.example.lab08.data.TaskDao
import com.example.lab08.reminders.TaskReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class CloudSyncManager(
    context: Context,
    private val dao: TaskDao,
    private val reminders: TaskReminderScheduler,
    private val scope: CoroutineScope
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val preferences = context.getSharedPreferences("cloud_sync", Context.MODE_PRIVATE)
    private val syncMutex = Mutex()
    private var registration: ListenerRegistration? = null
    private var activeUid: String? = null
    private val _status = MutableStateFlow("Nube: sin sesión")
    val status: StateFlow<String> = _status
    private val _signedIn = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedIn

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        _signedIn.value = uid != null
        if (uid == null) {
            registration?.remove()
            registration = null
            activeUid = null
            _status.value = "Nube: sin sesión"
        } else if (uid != activeUid) {
            startFor(uid)
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun signInWithGoogle(context: Context) {
        _status.value = "Nube: conectando…"
        scope.launch {
            try {
                val option = GetSignInWithGoogleOption.Builder(
                    context.getString(R.string.default_web_client_id)
                ).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val result = CredentialManager.create(context).getCredential(context, request)
                val credential = result.credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    _status.value = "Nube: respuesta de Google no reconocida"
                    return@launch
                }
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
            } catch (_: GetCredentialCancellationException) {
                _status.value = "Nube: sin sesión"
            } catch (error: Exception) {
                _status.value = "Nube: ${error.localizedMessage ?: "no se pudo iniciar sesión"}"
            }
        }
    }

    fun signOut(context: Context) {
        auth.signOut()
        scope.launch {
            try {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Firebase ya cerró la sesión; el proveedor puede no tener estado que limpiar.
            }
        }
    }

    fun requestSync() {
        val uid = activeUid ?: return
        scope.launch { synchronize(uid) }
    }

    fun close() {
        registration?.remove()
        auth.removeAuthStateListener(authListener)
    }

    private fun startFor(uid: String) {
        val owner = preferences.getString("owner_uid", null)
        if (owner != null && owner != uid) {
            _status.value = "Esta instalación ya está vinculada a otra cuenta"
            auth.signOut()
            return
        }
        preferences.edit().putString("owner_uid", uid).apply()
        registration?.remove()
        activeUid = uid
        _status.value = "Nube: sincronizando…"
        registration = collection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _status.value = "Nube: ${error.localizedMessage ?: "error de conexión"}"
            } else if (snapshot != null && !snapshot.metadata.isFromCache) {
                requestSync()
            }
        }
    }

    private suspend fun synchronize(uid: String) = syncMutex.withLock {
        if (auth.currentUser?.uid != uid) return@withLock
        try {
            _status.value = "Nube: sincronizando…"
            val collection = collection(uid)
            val remote = collection.get(Source.SERVER).await().documents
                .mapNotNull { document -> document.toTask()?.let { document.id to it } }
                .toMap()
            val local = dao.getAllForSync().associateBy { it.cloudId }

            for ((cloudId, remoteTask) in remote) {
                val localTask = local[cloudId]
                when {
                    localTask == null -> {
                        val id = dao.insertTask(remoteTask)
                        reminders.update(remoteTask.copy(id = id.toInt()))
                    }
                    remoteTask.updatedAt >= localTask.updatedAt -> {
                        val updated = remoteTask.copy(id = localTask.id)
                        if (updated != localTask) {
                            dao.updateTask(updated)
                            reminders.update(updated)
                        }
                    }
                    else -> collection.document(cloudId).set(localTask.toCloudMap()).await()
                }
            }
            for ((cloudId, localTask) in local) {
                if (cloudId !in remote) {
                    collection.document(cloudId).set(localTask.toCloudMap()).await()
                }
            }
            _status.value = "Nube: sincronizada"
        } catch (error: Exception) {
            _status.value = "Nube: ${error.localizedMessage ?: "sin conexión"}"
        }
    }

    private fun collection(uid: String) = firestore.collection("users").document(uid).collection("tasks")

    private fun DocumentSnapshot.toTask(): Task? {
        val description = getString("description") ?: return null
        return Task(
            description = description,
            isCompleted = getBoolean("isCompleted") ?: false,
            reminderAt = getLong("reminderAt"),
            cloudId = id,
            updatedAt = getLong("updatedAt") ?: 0L,
            isDeleted = getBoolean("isDeleted") ?: false
        )
    }

    private fun Task.toCloudMap(): Map<String, Any?> = mapOf(
        "description" to description,
        "isCompleted" to isCompleted,
        "reminderAt" to reminderAt,
        "updatedAt" to updatedAt,
        "isDeleted" to isDeleted
    )
}
