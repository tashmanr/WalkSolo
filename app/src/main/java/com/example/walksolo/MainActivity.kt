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
import android.app.Activity
import android.os.Build
import androidx.core.content.ContextCompat
import PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import PermissionUtils.isPermissionGranted
import PermissionUtils.requestPermission
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.widget.Toast
//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothAdapter

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent
    private var bluetoothIsEnabled: Boolean = false

    private var m_bluetoothAdapter: BluetoothAdapter ?= null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>

    companion object{
        private const val REQUEST_ENABLE_BT = 1
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
    @SuppressLint("MissingPermission")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.navigate -> {
                navigateIntent = Intent(this, MapsActivity::class.java)
                startActivity(navigateIntent)
            }
            R.id.aroundme -> {
                enableBluetooth()
                if(bluetoothIsEnabled){
                    showErrorMessage("in if")
                    pairedDeviceList()
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetooth(){
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            showErrorMessage("this device doesn't support bluetooth")
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
        }
        else{
            bluetoothIsEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun pairedDeviceList(){
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if(!m_pairedDevices.isEmpty()){
            for (device: BluetoothDevice in m_pairedDevices) {
                list.add(device)
                showErrorMessage(device.name)
            }
        }
        else{
            showErrorMessage("no paired devices found")
        }

    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    bluetoothIsEnabled = true
                    showErrorMessage("Bluetooth has been enabled")
                } else {
                    showErrorMessage("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showErrorMessage("Bluetooth enabling has been canceled")
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
