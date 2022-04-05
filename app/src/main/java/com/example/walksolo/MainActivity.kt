package com.example.walksolo

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothAdapter

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var status: TextView
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent
    private var mConnectedThread: ConnectedThread ?= null
    private var mConnectToDeviceThread: ConnectToDeviceThread ?= null

    companion object{
        private const val REQUEST_ENABLE_BT = 1
        var pairedRaspberryPi: BluetoothDevice ?= null
        var m_bluetoothAdapter: BluetoothAdapter ?= null
        lateinit var m_pairedDevices: Set<BluetoothDevice>
        var bluetoothIsEnabled: Boolean = false
        var m_myUUID: UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")
        var m_bluetoothSocket: BluetoothSocket? = null
        var m_isConnected: Boolean = false

        // Defines several constants used when transmitting messages between the
        // service and the UI.
        const val MESSAGE_READ: Int = 0
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_RECEIVED: Int = 2
        const val STATE_CONNECTING: Int = 3
        const val STATE_CONNECTON_FAILED: Int = 4
        const val STATE_CONNECTED: Int = 5
        const val STATE_CANNOT_WRITE = 6

    }

    val handler = object: Handler(Looper.getMainLooper()){

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_READ -> {
                    status.setText("MESSAGE_READ")
                }
                MESSAGE_WRITE -> {
                    status.setText("MESSAGE_WRITE")
                }
                MESSAGE_RECEIVED ->{
                    status.setText("MESSAGE_RECEIVED")
                }
                STATE_CONNECTING -> {
                    status.setText("STATE_CONNECTING")
                }
                STATE_CONNECTON_FAILED -> {
                    status.setText("STATE_CONNECTON_FAILED")
                }
                STATE_CONNECTED -> {
                    status.setText("STATE_CONNECTED")
                }
                STATE_CANNOT_WRITE -> {
                    status.setText("STATE_CANNOT_WRITE")
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateButton = findViewById(R.id.navigate)
        navigateButton.setOnClickListener(this)
        aroundMeButton = findViewById(R.id.aroundme)
        aroundMeButton.setOnClickListener(this)
        layout = findViewById(R.id.coordinatorLayout)
        status = findViewById(R.id.status)
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
                    // TODO check if already connected
                    mConnectToDeviceThread = ConnectToDeviceThread()
                    mConnectToDeviceThread!!.start()
                    //mConnectedThread!!.write("Hello".toByteArray())
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

        if(!m_pairedDevices.isEmpty()){
            for (device: BluetoothDevice in m_pairedDevices) {
                if (device.name.equals("raspberrypi", ignoreCase = true)){
                    pairedRaspberryPi = device
                    break
                }
            }
        }
        else{
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

    // Code for Bluetooth client to connect to bluetooth server socket.
    @SuppressLint("MissingPermission")
    private inner class ConnectToDeviceThread() : Thread(){

        private val m_clientSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            pairedRaspberryPi!!.createRfcommSocketToServiceRecord(m_myUUID)
        }

        public override fun run() {
            //Cancel the discovery process because it slow down the connection
            if(m_bluetoothAdapter?.isDiscovering == true) {
                m_bluetoothAdapter?.cancelDiscovery()
            }

            m_clientSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                try{
                    socket.connect()

                    var msg: Message = handler.obtainMessage(STATE_CONNECTED)
                    handler.sendMessage(msg)

                    // Start the thread to manage the connection and perform transmissions
                    mConnectedThread = ConnectedThread(socket)
                    mConnectedThread!!.start()
                }
                catch (e: IOException){
                    var msg: Message = handler.obtainMessage(STATE_CONNECTON_FAILED)
                    handler.sendMessage(msg)
                }
            }
        }

        fun cancel(){
            try {
                m_clientSocket?.close()
            }catch (e: IOException){
                Log.e("Socket", "Could not close the client socket",e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("TAG", "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("TAG", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(STATE_CANNOT_WRITE)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("TAG", "Could not close the connect socket", e)
            }
        }
    }
}
