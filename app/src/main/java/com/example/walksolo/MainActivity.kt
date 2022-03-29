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

import android.os.Build
import androidx.core.content.ContextCompat
import PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import PermissionUtils.isPermissionGranted
import PermissionUtils.requestPermission
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent

    companion object{
        private const val BT_SCAN_REQUEST_CODE = 100
        private const val BT_CONNECT_REQUEST_CODE = 101
    }
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
                enableBluetooth()
                showErrorMessage("Please tell me what's around me.")
            }
        }
    }

    /**
     * Enables if the BT_SCAN permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this,"permission BT SCAN is enabled",Toast.LENGTH_SHORT).show()
        } else {
            // Permission get bluetooth scan
            requestPermission(
                this, BT_SCAN_REQUEST_CODE,
                Manifest.permission.BLUETOOTH_SCAN, true
            )
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this,"permission BT CONNECT is enabled",Toast.LENGTH_SHORT).show()
        } else {
            // Permission to Bluetooth_CONNECT
            requestPermission(
                this, BT_CONNECT_REQUEST_CODE,
                Manifest.permission.BLUETOOTH_CONNECT, true
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == BT_SCAN_REQUEST_CODE){
            if (isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            ) {
                // the permission has been granted.
                Toast.makeText(this,"permission BT SCAN is enabled",Toast.LENGTH_SHORT).show()
            } else {
                // Permission was denied. Display an error message
                Toast.makeText(this,"permission BT SCAN DENIED",Toast.LENGTH_SHORT).show()
            }
        }
        else if(requestCode == BT_CONNECT_REQUEST_CODE){
            if (isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            ) {
                // the permission has been granted.
                Toast.makeText(this,"permission BT CONNECT is enabled",Toast.LENGTH_SHORT).show()
            } else {
                // Permission was denied. Display an error message
                Toast.makeText(this,"permission BT CONNECT DENIED",Toast.LENGTH_SHORT).show()
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
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
