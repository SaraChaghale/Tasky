package com.sachna.tasky.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.sachna.tasky.R
import com.sachna.tasky.activities.MainActivity
import com.sachna.tasky.adapters.OnClickListener
import com.sachna.tasky.adapters.TaskAdapter
import com.sachna.tasky.database.TaskApplication
import com.sachna.tasky.databinding.FragmentTypeTaskBinding
import com.sachna.tasky.entities.TaskEntity



class TypeTaskFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentTypeTaskBinding
    private lateinit var mAdapter: TaskAdapter
    private lateinit var taskType: String
    private lateinit var mGridLayout: GridLayoutManager
    private lateinit var originalTitle  :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskType = arguments?.getString("taskType") ?: "Sin tipo"


        this.setHasOptionsMenu(true)
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTypeTaskBinding.inflate(inflater, container, false)

        setupRecyclerView()


        showTasksByType(taskType)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        originalTitle= activity.supportActionBar?.title.toString()


        activity.supportActionBar?.apply {
            title = "My $taskType Tasks"
            setDisplayHomeAsUpEnabled(true) }
            (activity as MainActivity).binding.toolbar.setNavigationOnClickListener {
                if (isAdded && activity != null) {
                    parentFragmentManager.popBackStack()

                    (requireActivity() as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                } else {
                    Log.e("TypeTaskFragment", "El fragmento ya no está asociado a la actividad")
                }




    }
        binding.addtask.setOnClickListener {
            launchEditFragment()
        }
    }


    private fun setupRecyclerView() {
        mAdapter = TaskAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(requireContext(), 2)
        getTaskbyType(taskType)
        binding.recyclerViewtype.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }


    private fun showTasksByType(type: String) {
        Thread {
            val tasks = TaskApplication.database.taskDao().getTaskByType(type)
            requireActivity().runOnUiThread {
                if (tasks.isEmpty()) {

                    binding.recyclerViewtype.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {

                    binding.recyclerViewtype.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    mAdapter.setTasks(tasks.toMutableList())
                }
            }
        }.start()
    }


    private fun getTaskbyType(type: String) {
        Thread {
            val tasks = TaskApplication.database.taskDao().getTaskByType(type)
            requireActivity().runOnUiThread {
                if (tasks.isEmpty()) {
                    binding.recyclerViewtype.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewtype.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    mAdapter.setTasks(tasks.toMutableList())
                }
            }
        }.start()
    }


    private fun launchEditFragment(args: Bundle? = null) {
        val fragment = MyTaskFragment()
        if (args != null) fragment.arguments = args

        parentFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.apply {
            title =  originalTitle
        }
    }

    override fun onClick(TaskId: Long) {
        val args = Bundle()
        args.putLong("id", TaskId)
        launchEditFragment(args)
    }


    override fun onDoneTask(taskEntity: TaskEntity) {
        taskEntity.isDone = !taskEntity.isDone
        Thread {
            TaskApplication.database.taskDao().updateTask(taskEntity)
            requireActivity().runOnUiThread {
                mAdapter.update(taskEntity)
            }
        }.start()
    }


    override fun onDeleteTask(taskEntity: TaskEntity) {
        Thread {
            TaskApplication.database.taskDao().deleteTask(taskEntity)
            requireActivity().runOnUiThread {
                mAdapter.delete(taskEntity)
            }
        }.start()
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_search)?.isVisible = false
    }


    override fun onDestroyView() {
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.apply {
            title = originalTitle}
            super.onDestroyView()
        (requireActivity() as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        requireActivity().invalidateOptionsMenu()
    }
}
