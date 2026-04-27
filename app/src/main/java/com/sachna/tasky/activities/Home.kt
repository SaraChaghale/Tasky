package com.sachna.tasky.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.sachna.tasky.R
import com.sachna.tasky.api.QuoteResponse
import com.sachna.tasky.api.RetrofitInstance
import com.sachna.tasky.databinding.HomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit

class Home : AppCompatActivity() {
    private lateinit var binding: HomeBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences
    private var userName: String = "Guest"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)


        userName = sharedPreferences.getString("userName", "Guest") ?: "Guest"

        if (userName != "Guest") {

            Log.d("GoogleSignIn", "Inicio automático con usuario guardado: $userName")
        } else {

            setupGoogleSignIn()
            checkGoogleAccount()
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

        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.next.setOnClickListener {
            val intent= Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", userName)
            startActivity(intent)
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun checkGoogleAccount() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {

            showSignInDialog(account.displayName ?: "Usuario")
        } else {

            showSignInDialog(null)
        }
    }

    private fun showSignInDialog(existingUserName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Iniciar sesión")

        if (existingUserName != null) {
            builder.setMessage("Se ha detectado una cuenta de Google: $existingUserName\n¿Quieres iniciar sesión con esta cuenta?")
        } else {
            builder.setMessage("No se detectó ninguna cuenta de Google.\n¿Quieres iniciar sesión?")
        }

        builder.setPositiveButton("Sí") { _: DialogInterface, _: Int ->
            signIn()
        }

        builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
            saveUserName("Guest")
        }

        builder.show()
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val name = account?.displayName ?: "Guest"
                saveUserName(name)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Error al iniciar sesión: ${e.statusCode}")
                saveUserName("Guest")
            }
        }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun saveUserName(name: String) {
        userName = name
        sharedPreferences.edit { putString("userName", name) }
        Log.d("GoogleSignIn", "Usuario guardado en SharedPreferences: $userName")
    }
}
