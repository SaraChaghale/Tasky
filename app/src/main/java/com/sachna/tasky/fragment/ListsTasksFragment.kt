package com.sachna.tasky.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

import com.sachna.tasky.R
import com.sachna.tasky.activities.MainActivity
import com.sachna.tasky.api.QuoteResponse
import com.sachna.tasky.api.RetrofitInstance
import com.sachna.tasky.database.TaskApplication
import com.sachna.tasky.databinding.FragmentListsTasksBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ListsTasksFragment : Fragment() {

    private lateinit var binding: FragmentListsTasksBinding


    private lateinit var mMap: GoogleMap
    private lateinit var originalTitle  :String
    private var previousToolbarTitle: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListsTasksBinding.inflate(inflater, container, false)

        view?.findViewById<Toolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        RetrofitInstance.api.getDailyQuote().enqueue(object : Callback<List<QuoteResponse>> {
            override fun onResponse(call: Call<List<QuoteResponse>>, response: Response<List<QuoteResponse>>) {
                if (response.isSuccessful) {
                    val quote = response.body()?.firstOrNull()
                    quote?.let {
                        binding.tvQuote.text = ("\"${it.q}\" - ${it.a}")
                    }
                } else {
                    binding.tvQuote.text = R.string.Nosepudoobtenerlacita.toString()
                }
            }

            override fun onFailure(call: Call<List<QuoteResponse>>, t: Throwable) {
                binding.tvQuote.text = ("Error: ${t.message}")
            }
        })
        setHasOptionsMenu(true)
        val orig = arguments?.getString("toolbar_title")?:"Tasky"
        val activity = activity as MainActivity

        activity.updateToolbarTitle(orig)

        setCardViewListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity


        activity.supportActionBar?.apply {
            title = getString(R.string.mytasklist)
            setDisplayHomeAsUpEnabled(true)
        }


        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }

        setCardViewListeners()
        setupMap()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.action_search)?.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)}


    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.map, it)
                    .commit()
            }

        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            lifecycleScope.launch {

                val taskList = withContext(Dispatchers.IO) {
                    TaskApplication.database.taskDao().getAllTasks()
                }

                if (taskList.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()

                    for (task in taskList) {
                        val pos = LatLng(task.latitude, task.longitude)
                        setMarker(
                            pos,
                            "Task ${task.name}",
                            "${task.address}, ${task.latitude}, ${task.longitude}"
                        )
                        boundsBuilder.include(pos)
                    }


                    val bounds = boundsBuilder.build()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // 100 es el padding.
                }
            }
        }
    }


    private fun setMarker(position: LatLng, titulo: String, info: String) {
        mMap.addMarker(
            MarkerOptions().position(position)
                .title(titulo)
                .snippet(info)
        )
    }

    private fun setCardViewListeners() {
        val cardMap = mapOf(
            binding.family to "Family",
            binding.relation to "Relation",
            binding.friends to "Friends",
            binding.selfcare to "Self Care",
            binding.home to "Home",
            binding.work to "Work",
            binding.funny to "Fun",
            binding.study to "Study",
            binding.other to "Other"
        )


        cardMap.forEach { (card, type) ->
            card.setOnClickListener {
                val fragment = TypeTaskFragment()
                val args = Bundle()
                args.putString("taskType", type)
                fragment.arguments = args


                parentFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity() as MainActivity
        activity.updateToolbarTitle(activity.orig)

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Activamos el botón de "volver atrás"

        activity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        activity.drawerLayout.openDrawer(GravityCompat.START)

        activity.binding.addtask.show()

        activity.title= previousToolbarTitle
        requireActivity().invalidateOptionsMenu()
    }
}



