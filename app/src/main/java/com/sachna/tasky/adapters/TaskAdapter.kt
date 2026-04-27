package com.sachna.tasky.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sachna.tasky.R
import com.sachna.tasky.databinding.ItemTaskBinding
import com.sachna.tasky.entities.TaskEntity

class TaskAdapter(
    private var TaskEntities: MutableList<TaskEntity>,
    private var listener: OnClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private lateinit var mContext: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = TaskEntities.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val Task = TaskEntities[position]
        with(holder) {
            setListener(Task)
            binding.tvname.text = Task.name
            binding.cbFavorite.isChecked = Task.isDone

            Glide.with(mContext)
                .load(Task.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgPhoto)
        }
    }


    fun add(TaskEntity: TaskEntity) {
        TaskEntities.add(TaskEntity)
        notifyDataSetChanged()
    }


    fun setTasks(Tasks: MutableList<TaskEntity>) {
        this.TaskEntities = Tasks
        notifyDataSetChanged()
    }

    fun update(TaskEntity: TaskEntity) {
        val index = TaskEntities.indexOf(TaskEntity)
        if (index != -1) {
            TaskEntities[index] = TaskEntity
            notifyItemChanged(index)
        }
    }


    fun delete(TaskEntity: TaskEntity) {
        val index = TaskEntities.indexOf(TaskEntity)
        if (index != -1) {
            TaskEntities.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemTaskBinding.bind(view)


        fun setListener(TaskEntity: TaskEntity) {
            with(binding.root) {
                setOnClickListener {
                    listener.onClick(TaskEntity.id)
                }

                setOnLongClickListener {
                    listener.onDeleteTask(TaskEntity)
                    true
                }
            }

            binding.cbFavorite.setOnClickListener {
                listener.onDoneTask(TaskEntity)
            }
        }
    }
}
