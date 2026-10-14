package com.example.lab08.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY id DESC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY id DESC")
    fun observeTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllForSync(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Query("SELECT * FROM tasks WHERE cloud_id = :cloudId LIMIT 1")
    suspend fun getByCloudId(cloudId: String): Task?

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET is_deleted = 1, reminder_at = NULL, updated_at = max(updated_at + 1, :updatedAt) WHERE is_deleted = 0")
    suspend fun deleteAllTasks(updatedAt: Long)
}
