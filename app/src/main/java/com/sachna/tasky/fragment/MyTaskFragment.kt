package com.sachna.tasky.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

import com.sachna.tasky.R
import com.sachna.tasky.activities.MainActivity
import com.sachna.tasky.database.TaskApplication

import com.sachna.tasky.databinding.FragmentMyTaskBinding
import com.sachna.tasky.entities.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


class MyTaskFragment : Fragment() {
    private var previousToolbarTitle: String? = null


    private var mActivity: MainActivity? = null
    private var isEditMode: Boolean = false
    private var mTaskEntity: TaskEntity? = null
    private lateinit var mBinding: FragmentMyTaskBinding
    private var selectedStatus: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        // Inflamos el layout usando View Binding
        mBinding = FragmentMyTaskBinding.inflate(inflater, container, false)

        // Cuando el usuario hace clic en el campo de fecha, mostramos el DatePicker
        mBinding.etDate.setOnClickListener {
            showDatePicker(mBinding.etDate)
        }
        setHasOptionsMenu(true)
        val orig = arguments?.getString("toolbar_title")?:"Tasky"
        val activity = activity as MainActivity
          // Guardar el título actual
        activity.updateToolbarTitle(orig)

        // Creamos el adaptador para el spinner usando un array de recursos
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.task_status_array, // Array definido en strings.xml
            android.R.layout.simple_spinner_item // Layout simple para cada elemento
        )
        // Definimos el layout del menú desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Asignamos el adaptador al spinner
        mBinding.spinnerOptions.adapter = adapter

        // Configuramos el listener para cuando se selecciona una opción en el spinner
        mBinding.spinnerOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // Cuando el usuario selecciona un elemento
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Obtenemos el texto seleccionado
                val selectedText = parent.getItemAtPosition(position).toString()

                // Asignamos el valor a selectedStatus según el texto seleccionado
                selectedStatus = when (selectedText) {
                    getString(R.string.family) -> "Family"
                    getString(R.string.relation) -> "Relation"
                    getString(R.string.friends) -> "Friends"
                    getString(R.string.self_care) -> "Self Care"
                    getString(R.string.home) -> "Home"
                    getString(R.string.work) -> "Work"
                    getString(R.string.funny) -> "Fun"
                    getString(R.string.study) -> "Study"
                    getString(R.string.other) -> "Other"
                    else -> ""
                }

                // Mostramos un mensaje informativo con la selección
                Snackbar.make(mBinding.root, "Selected: $selectedText", Snackbar.LENGTH_SHORT).show()
            }

            // Cuando no se selecciona nada, vaciamos selectedStatus
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedStatus = ""
            }
        }

        // Devolvemos la vista raíz del layout
        return mBinding.root
    }

    // Muestra un DatePickerDialog para seleccionar una fecha
    private fun showDatePicker(view: TextInputEditText) {
        val calendar = Calendar.getInstance() // Obtenemos la fecha actual
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Creamos el diálogo de selección de fecha
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formateamos la fecha seleccionada y la mostramos en el campo de texto
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                view.setText(selectedDate)
            },
            year, month, day
        )
        // Mostramos el diálogo
        datePicker.show()
    }
    private var originalTitle: String? = null


    // Método que se ejecuta después de que la vista ha sido creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalTitle = mActivity?.supportActionBar?.title.toString()

        // Obtenemos el ID de la tarea pasada en los argumentos (si existe)
        val id = arguments?.getLong("id", 0)

        if (id != null && id != 0L) {
            isEditMode = true // Si hay un ID, estamos en modo edición
            getTask(id) // Obtenemos los datos de la tarea
        } else {
            isEditMode = false // Si no, es una nueva tarea
            // Creamos una nueva tarea vacía
            mTaskEntity = TaskEntity(
                name = "",
                description = "",
                date = Date(),
                address = "",
                latitude = 0.0,
                longitude = 0.0,
                photoUrl = "",
                type = ""
            )
        }

        // Asignamos la actividad principal
        mActivity = activity as? MainActivity
        if (originalTitle == null) {
            originalTitle = mActivity?.supportActionBar?.title?.toString()
        }


        // Habilitamos el botón "atrás" en la barra de acción
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Cambiamos el título según estemos editando o creando
        if (isEditMode) {
            mActivity?.supportActionBar?.title = getString(R.string.edi_store_title)
        } else {
            mActivity?.supportActionBar?.title = getString(R.string.add_store_title)
        }
        (activity as MainActivity).binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

// Bloqueamos el Drawer para que no se abra
        (requireActivity() as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        // Indicamos que este fragmento tiene un menú propio
        setHasOptionsMenu(true)

        // Cuando cambia el texto de la URL de la foto, cargamos la imagen con Glide
        mBinding.etPhotoUrl.addTextChangedListener {
            Glide.with(this)
                .load(mBinding.etPhotoUrl.text.toString()) // URL de la foto
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cachear la imagen
                .centerCrop() // Recortar y centrar la imagen
                .into(mBinding.imgPhoto) // Mostrar en el ImageView
        }
    }

    // Recupera una tarea de la base de datos usando su ID
    private fun getTask(id: Long) {
        Thread {
            // Obtenemos la tarea usando el DAO de Room
            mTaskEntity = TaskApplication.database.taskDao().getTaskById(id)
            mTaskEntity?.let {
                // Ejecutamos la actualización de la UI en el hilo principal
                requireActivity().runOnUiThread {
                    setUiTask(it)
                }
            }
        }.start()
    }

    // Llena los campos de la interfaz con los datos de la tarea
    private fun setUiTask(taskEntity: TaskEntity) {
        with(mBinding) {
            etname.setText(taskEntity.name)
            etdescrip.setText(taskEntity.description)
            etDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(taskEntity.date))
            etAddress.setText(taskEntity.address)
            etLatitude.setText(taskEntity.latitude.toString())
            etLongitude.setText(taskEntity.longitude.toString())
            etPhotoUrl.setText(taskEntity.photoUrl)

            // Cargamos la foto con Glide
            activity?.let {
                Glide.with(it)
                    .load(taskEntity.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imgPhoto)
            }

            // Seleccionamos la opción del spinner correspondiente al tipo de tarea
            val statusArray = resources.getStringArray(R.array.task_status_array)
            val position = statusArray.indexOf(taskEntity.type)
            if (position != -1) {
                mBinding.spinnerOptions.setSelection(position)
                selectedStatus = taskEntity.type // Sincronizamos el estado seleccionado
            }
        }
    }

    // Sobrescribimos el método para crear el menú de opciones
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflamos el menú desde el archivo de recursos edit_task_menu.xml
        inflater.inflate(R.menu.edit_task_menu, menu)
         menu.findItem(R.id.action_search)?.isVisible = false
        // Llamamos al método de la superclase
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Sobrescribimos el método que gestiona las acciones cuando se selecciona una opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Cuando se pulsa el botón de "Atrás" (flecha en la barra superior)
            android.R.id.home -> {
                mActivity?.onBackPressedDispatcher?.onBackPressed() // Volvemos a la pantalla anterior
                true // Indicamos que se ha manejado el evento
            }

            // Cuando se selecciona la opción de eliminar la tarea
            R.id.nav_delete -> {
                // Mostramos un cuadro de diálogo de confirmación
                AlertDialog.Builder(context)
                    .setTitle(R.string.DeleteTask) // Título del cuadro de diálogo
                    .setMessage(R.string.aresure) // Mensaje de confirmación
                    .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia
                    .setPositiveButton(R.string.OK) { _, _ ->
                        deleteTask() // Si confirma, se elimina la tarea
                    }
                    .setNegativeButton(R.string.Cancel) { dialog, _ ->
                        dialog.dismiss() // Si cancela, se cierra el cuadro de diálogo
                    }
                    .show()
                true // Indicamos que se ha manejado el evento
            }

            // Cuando se selecciona la opción de guardar la tarea
            R.id.nav_save -> {
                if (mTaskEntity != null) { // Verificamos que la tarea no sea nula
                    with(mTaskEntity!!) { // Usamos el objeto mTaskEntity directamente
                        name = mBinding.etname.text.toString().trim() // Guardamos el nombre
                        description = mBinding.etdescrip.text.toString().trim() // Guardamos la descripción

                        // Formateador de fechas
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateString = mBinding.etDate.text.toString().trim()

                        // Convertimos el texto de la fecha a un objeto Date, si hay error, usamos la fecha actual
                        date = if (dateString.isNotEmpty()) {
                            try {
                                dateFormat.parse(dateString)
                            } catch (e: ParseException) {
                                Date() // Fecha actual si el formato es incorrecto
                            }
                        } else {
                            Date() // Fecha actual si el campo está vacío
                        }

                        address = mBinding.etAddress.text.toString().trim() // Dirección
                        latitude = mBinding.etLatitude.text.toString().trim().toDoubleOrNull() ?: 0.0 // Latitud
                        longitude = mBinding.etLongitude.text.toString().trim().toDoubleOrNull() ?: 0.0 // Longitud
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim() // URL de la foto
                        type = selectedStatus // Estado/tipo de tarea
                    }
                }

                // Creamos una cola para sincronizar la operación de base de datos
                val queue = LinkedBlockingQueue<TaskEntity>()
                Thread {
                    if (isEditMode) { // Si estamos editando
                        TaskApplication.database.taskDao().updateTask(mTaskEntity!!) // Actualizamos la tarea
                    } else { // Si es una nueva tarea
                        mTaskEntity?.id = TaskApplication.database.taskDao().addTask(mTaskEntity!!) // Añadimos la tarea
                    }
                    queue.add(mTaskEntity!!) // Añadimos la tarea a la cola
                }.start()

                // Tomamos la tarea de la cola y actualizamos la UI en el hilo principal
                with(queue.take()) {
                    if (isEditMode) { // Si es una edición
                        mActivity?.updateTask(this) // Actualizamos la tarea en la lista
                        Snackbar.make(mBinding.root, R.string.modify, Snackbar.LENGTH_SHORT).show() // Mensaje de éxito
                    } else { // Si es una nueva tarea
                        mActivity?.addTask(this) // Añadimos la tarea a la lista
                        Snackbar.make(mBinding.root, R.string.AddTask, Snackbar.LENGTH_SHORT).show() // Mensaje de éxito
                    }
                }
                true // Indicamos que se ha manejado el evento
            }

            // Si no se reconoce la opción, pasamos el evento al método de la superclase
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Método para eliminar una tarea
    private fun deleteTask() {
        mTaskEntity?.let { taskEntity -> // Verificamos que la tarea no sea nula
            CoroutineScope(Dispatchers.IO).launch { // Lanzamos una corrutina en el hilo de entrada/salida
                TaskApplication.database.taskDao().deleteTask(taskEntity) // Eliminamos la tarea de la base de datos
                withContext(Dispatchers.Main) { // Cambiamos al hilo principal para actualizar la UI
                    Snackbar.make(mBinding.root, R.string.TaskEliminated, Snackbar.LENGTH_SHORT).show() // Mensaje de éxito
                    requireActivity().onBackPressedDispatcher.onBackPressed() // Volvemos a la pantalla anterior
                }
            }
        }
    }



    //Cuando destruyo la vista, se restablece a como estaba antes
    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity() as MainActivity
        activity.updateToolbarTitle(activity.orig)

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Activamos el botón de "volver atrás"
   // Restauramos el ícono del Drawer

        // Desbloquear el Drawer
        activity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        // Hacer visible el Drawer de nuevo (si estaba cerrado)
        // Si deseas dejar el Drawer cerrado, puedes eliminar esta línea
        activity.drawerLayout.openDrawer(GravityCompat.START)

        mActivity?.binding?.addtask?.show()

        mActivity?.title= previousToolbarTitle
        requireActivity().invalidateOptionsMenu()
    }
}
