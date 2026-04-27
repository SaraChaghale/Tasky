
package com.sachna.tasky.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sachna.tasky.entities.TaskEntity
import java.util.Date

// @Dao indica que esta interfaz es un Data Access Object (DAO), o sea, el intermediario entre la app y la base de datos.
@Dao
interface TaskDAO {

    // Recupera todas las tareas de la base de datos. Devuelve una lista mutable.
    @Query("SELECT * FROM TaskEntity")
    fun getAllTasks(): MutableList<TaskEntity>

    // Recupera una tarea específica por su ID.
    @Query("SELECT * FROM TaskEntity WHERE id= :id")
    fun getTaskById(id: Long): TaskEntity

    // Recupera las tareas completadas o no completadas según el valor de 'isDone'.
    @Query("SELECT * FROM TaskEntity WHERE isDone = :isDone")
    fun getDoneTasks(isDone: Boolean): List<TaskEntity>

    // Recupera las tareas dentro de un rango de fechas (entre startDate y endDate).
    @Query("SELECT * FROM TaskEntity WHERE date BETWEEN :startDate AND :endDate AND isDone = :isDone")
    fun getProperTask(startDate: Date, endDate: Date, isDone: Boolean = false): List<TaskEntity>

    // Recupera las tareas según el tipo (por ejemplo, "trabajo", "personal", etc.).
    @Query("SELECT * FROM TaskEntity WHERE type = :type")
    fun getTaskByType(type: String): List<TaskEntity>

    // Recupera las tareas según el nombre (o título) exacto.
    @Query("SELECT * FROM TaskEntity WHERE name = :name")
    fun getTaskByName(name: String): List<TaskEntity>

    // Inserta una nueva tarea en la base de datos y devuelve el ID de la tarea recién creada.
    @Insert
    fun addTask(taskEntity: TaskEntity): Long

    // Actualiza una tarea existente en la base de datos.
    @Update
    fun updateTask(taskEntity: TaskEntity)

    // Elimina una tarea de la base de datos.
    @Delete
    fun deleteTask(taskEntity: TaskEntity)
}
