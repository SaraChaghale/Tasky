package com.sachna.tasky.adapters

import com.sachna.tasky.entities.TaskEntity
interface OnClickListener {

    fun onClick(TaskId: Long)


    fun onDoneTask(taskEntity: TaskEntity)


    fun onDeleteTask(taskEntity: TaskEntity)
}
