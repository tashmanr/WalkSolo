package com.example.walksolo.settings

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.walksolo.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Settings"
        if (findViewById<FrameLayout>(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return
            }
            fragmentManager.beginTransaction().add(R.id.fragment_container, SettingsFragment())
                .commit()
        }
    }
}