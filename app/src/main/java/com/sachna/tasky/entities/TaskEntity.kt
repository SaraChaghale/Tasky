package com.sachna.tasky.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "TaskEntity")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var name: String,
    var type: String,
    var description: String = "",
    var date: Date,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var photoUrl: String,

    var isDone: Boolean = false
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskEntity

        return id == other.id
    }


    override fun hashCode(): Int {
        return id.hashCode()
    }
}
