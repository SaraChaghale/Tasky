package com.sachna.tasky.activities


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import com.sachna.tasky.R
import com.sachna.tasky.adapters.OnClickListener
import com.sachna.tasky.adapters.TaskAdapter
import com.sachna.tasky.database.TaskApplication
import com.sachna.tasky.databinding.ActivityMainBinding
import com.sachna.tasky.entities.TaskEntity
import com.sachna.tasky.fragment.ListsTasksFragment
import com.sachna.tasky.fragment.MyTaskFragment
import com.sachna.tasky.fragment.SettingsFragment
import java.util.Calendar
import java.util.Date
import java.util.concurrent.LinkedBlockingQueue



class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    MainAux,
    OnClickListener

{

    lateinit var binding: ActivityMainBinding
    lateinit var drawerLayout: DrawerLayout
    private lateinit var mAdapter: TaskAdapter
    private lateinit var mGridLayout: GridLayoutManager
    lateinit var orig : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupRecyclerView()


        binding.addtask.setOnClickListener {
            launchEditFragment()
        }




        drawerLayout = findViewById(R.id.drawer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)


        if (savedInstanceState == null) {
            getProperTask()
            navigationView.setCheckedItem(R.id.nav_home)
        }



        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homev -> {
                    getProperTask()
                    toolbar.title = "Tasky"
                    true
                }
                R.id.CompletedTask -> {
                    getDoneTask()
                    toolbar.title = getString(R.string.completed_task)
                    true

                }
                R.id.ViewAllask -> {
                    getTask()
                    toolbar.title = getString(R.string.all_task)
                    true
                }
                R.id.MyList -> {
                    getListTasks()
                    true
                }
                else -> false
            }
        }
        orig= toolbar.title.toString()
    }


    private fun getListTasks(args: Bundle? = null) {

        val fragment = ListsTasksFragment()


        val toolbarTitle = supportActionBar?.title?.toString() ?: orig


        val bundle = args ?: Bundle()
        bundle.putString("toolbar_title", toolbarTitle)

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .add(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()

        hideFav()
    }


    private fun launchEditFragment(args: Bundle? = null) {
        val fragment = MyTaskFragment()

        val toolbarTitle = supportActionBar?.title?.toString() ?: orig


        if (args != null) fragment.arguments = args


        supportFragmentManager.beginTransaction()
            .add(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()

        hideFav()
    }

    private fun getDoneTask() {
        val queue = LinkedBlockingQueue<MutableList<TaskEntity>>()

        Thread {
            val tasks = TaskApplication.database.taskDao().getDoneTasks(true)
            queue.add(tasks.toMutableList())
        }.start()

        mAdapter.setTasks(queue.take())
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = R.string.search_task.toString()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchTaskByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    getTask()
                }
                return true
            }
        })

        return true
    }


    private fun searchTaskByName(name: String) {
        val queue = LinkedBlockingQueue<MutableList<TaskEntity>>()
        Thread {
            val taskDao = TaskApplication.database.taskDao().getTaskByName(name)
            queue.add(taskDao.toMutableList())
        }.start()

        mAdapter.setTasks(queue.take())
    }


    private fun setupRecyclerView() {
        mAdapter = TaskAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, 2)
        getProperTask()
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }


    private fun getProperTask() {
        val calendar = Calendar.getInstance().apply {
            val targetDate = Date()
            time = targetDate
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val startDate = calendar.time
        val targetDate = Date()
        calendar.time = targetDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endDate = calendar.time
        val queue = LinkedBlockingQueue<MutableList<TaskEntity>>()
        Thread {
            val tasks = TaskApplication.database.taskDao().getProperTask(startDate, endDate,false)
            queue.add(tasks.toMutableList())
        }.start()
        mAdapter.setTasks(queue.take())
    }

    private fun getTask() {
        val queue = LinkedBlockingQueue<MutableList<TaskEntity>>()


        Thread {
            val tasks = TaskApplication.database.taskDao().getAllTasks()
            queue.add(tasks)
        }.start()

        mAdapter.setTasks(queue.take())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.set -> supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, SettingsFragment()).commit()

            R.id.nav_logout -> finishAffinity()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)}

        else if (supportFragmentManager.backStackEntryCount > 0) {
                binding.toolbar.title = orig
                supportFragmentManager.popBackStack()

            } else {
                super.onBackPressed()
            }
        }


    fun updateToolbarTitle(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        orig = toolbar.title.toString()
        toolbar.title = title}

    override fun hideFav(isVisible: Boolean) {
        if (isVisible) binding.addtask.show() else binding.addtask.hide()
    }

    override fun hideBottom(isVisible: Boolean) {

    }


    override fun onClick(TaskId: Long) {
        val args = Bundle()
        args.putLong("id", TaskId)
        launchEditFragment(args)
    }


    override fun onDoneTask(taskEntity: TaskEntity) {
        taskEntity.isDone = !taskEntity.isDone
        val queue = LinkedBlockingQueue<TaskEntity>()


        Thread {
            TaskApplication.database.taskDao().updateTask(taskEntity)
            queue.add(taskEntity)
        }.start()

        mAdapter.update(queue.take())
    }

    override fun onDeleteTask(taskEntity: TaskEntity) {
        val queue = LinkedBlockingQueue<TaskEntity>()

        Thread {
            TaskApplication.database.taskDao().deleteTask(taskEntity)
            queue.add(taskEntity)
        }.start()

        mAdapter.delete(queue.take())
    }

    override fun addTask(taskEntity: TaskEntity) {
        mAdapter.add(taskEntity)
    }

    fun updateTask(taskEntity: TaskEntity) {
        mAdapter.update(taskEntity)
    }
}
