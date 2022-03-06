package com.example.walksolo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateButton = findViewById(R.id.navigate)
        navigateButton.setOnClickListener(this)
        aroundMeButton = findViewById(R.id.aroundme)
        aroundMeButton.setOnClickListener(this)
        layout = findViewById(R.id.coordinatorLayout)
    }

    //function that waits for a button to be pressed when pressed will execute the following code
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.navigate -> {
                navigateIntent = Intent(this, MapsActivity::class.java)
                startActivity(navigateIntent)
            }
            R.id.aroundme -> {
                showErrorMessage("Please tell me what's around me.")
            }
        }
    }

    //function for showing the appropriate error message depending upon the error
    @SuppressLint("ShowToast")
    fun showErrorMessage(s: String) {
        Snackbar.make(layout, s, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(Color.GRAY).show()
    }
}