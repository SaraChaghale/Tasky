package com.sachna.tasky.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.sachna.tasky.R
import com.sachna.tasky.database.TaskApplication
import com.sachna.tasky.databinding.ActivityMapsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        lifecycleScope.launch {

            val taskList = withContext(Dispatchers.IO) {
                TaskApplication.database.taskDao().getAllTasks() // Sacamos todas las tareas.
            }


            for (task in taskList) {
                val pos = LatLng(task.longitude, task.latitude)


                setMarker(
                    pos,
                    "task ${task.id}", // Título del marcador.
                    " ${task.address}, ${task.latitude}, ${task.longitude}" // Información extra del marcador.
                )

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 11.5f)) // Zoom a nivel de ciudad.
            }
        }
    }

    // Función para crear un marcador en el mapa.
    private fun setMarker(position: LatLng, titulo: String, info: String) {
        mMap.addMarker(
            MarkerOptions().position(position)
                .title(titulo)
                .snippet(info)
        )

        //En realidad no usé esto, porque luego puse el mapa en un fragment, pero igual lo deje implementado.
    }
}
