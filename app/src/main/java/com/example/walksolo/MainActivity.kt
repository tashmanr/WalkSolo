package com.example.walksolo

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothAdapter

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var status: TextView
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var notifyMeButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent
    private var connected: Boolean = false
    private var mBluetoothService: BluetoothService? = null
    private var mImageSaver: ImageSaver = ImageSaver(this, 0)

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        var pairedRaspberryPi: BluetoothDevice? = null
        var m_bluetoothAdapter: BluetoothAdapter? = null
        lateinit var m_pairedDevices: Set<BluetoothDevice>
        var bluetoothIsEnabled: Boolean = false
    }

    private val handler = object : Handler(Looper.getMainLooper()) {

        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothService.STATE_CONNECTED -> {
                            status.text = "Connected"
                            connected = true
                        }
                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    //"""val writeMessage = String(writeBuf)"""
                    showErrorMessage("Received Image")
                }
                Constants.MESSAGE_READ -> {
                    // Permission to access the storage is missing. Show rationale and request permission
                    val readBuf = msg.obj as ByteArray
//                    val path = mImageSaver.saveImage(readBuf)
                    // construct a string from the valid bytes in the buffer
                    callVisionAPI(readBuf)
                    showErrorMessage("Got back from API call!")
                }
                Constants.MESSAGE_TOAST -> {
                    status.text = "not_connected"
                    connected = false
                }
            }
        }

    }

    fun callVisionAPI(imageArray: ByteArray){
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        GoogleVisionAPIHandler().detectLocalizedObjects(imageArray)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateButton = findViewById(R.id.navigate)
        navigateButton.setOnClickListener(this)
        aroundMeButton = findViewById(R.id.aroundme)
        aroundMeButton.setOnClickListener(this)
        notifyMeButton = findViewById(R.id.notify_me)
        notifyMeButton.setOnClickListener(this)
        layout = findViewById(R.id.coordinatorLayout)
        status = findViewById(R.id.status)
        mBluetoothService = BluetoothService(handler)
        enableBluetooth()
        if (bluetoothIsEnabled) {
            checkDeviceList()
            if (pairedRaspberryPi != null) {
                mBluetoothService?.connect(pairedRaspberryPi)

            }
        }
    }

    //function that waits for a button to be pressed when pressed will execute the following code
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.navigate -> {
                navigateIntent = Intent(this, MapsActivity::class.java)
                startActivity(navigateIntent)
            }
            R.id.aroundme -> {
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
                            status.text = "In if State not connected"
                            showErrorMessage("Not Connected")
                        }
                        val send = "1".toByteArray()
                        mBluetoothService?.write(send)

                        // TODO check if already connected
                        //mConnectToDeviceThread = ConnectToDeviceThread()
                        //mConnectToDeviceThread!!.start()
                        //mConnectedThread!!.write("Hello".toByteArray())
                    }
                }
            }
            R.id.notify_me -> {
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
                            status.text = "In if 2 State not connected"
                            showErrorMessage("Not Connected")
                        }
                        val send = "2".toByteArray()
                        mBluetoothService?.write(send)

                        // TODO check if already connected
                        //mConnectToDeviceThread = ConnectToDeviceThread()
                        //mConnectToDeviceThread!!.start()
                        //mConnectedThread!!.write("Hello".toByteArray())
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (m_bluetoothAdapter == null) {
            showErrorMessage("this device doesn't support bluetooth")
            return
        }
        if (!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
        } else {
            bluetoothIsEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceList() {
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices

        if (!m_pairedDevices.isEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                if (device.name.equals("raspberrypi", ignoreCase = true)) {
                    pairedRaspberryPi = device
                    break
                }
            }
        } else {
            showErrorMessage("no paired devices found")
            return
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
