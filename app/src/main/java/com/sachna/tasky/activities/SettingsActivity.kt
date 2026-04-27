package com.sachna.tasky.activities

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sachna.tasky.fragment.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Usamos un Fragment que manejará las preferencias
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, SettingsFragment())
            .commit()
    }

}
