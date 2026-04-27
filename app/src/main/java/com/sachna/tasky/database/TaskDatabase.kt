package com.sachna.tasky.database


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sachna.tasky.activities.Converters
import com.sachna.tasky.dao.TaskDAO
import com.sachna.tasky.entities.TaskEntity


@Database(entities = arrayOf(TaskEntity::class), version = 2)


@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {


    abstract fun taskDao(): TaskDAO
}
