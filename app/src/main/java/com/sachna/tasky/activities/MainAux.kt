package com.sachna.tasky.activities

import com.sachna.tasky.entities.TaskEntity


interface MainAux {


    fun hideFav(isVisible: Boolean = false)

    fun hideBottom(isVisible: Boolean = false)

    fun addTask(taskEntity: TaskEntity)
}
