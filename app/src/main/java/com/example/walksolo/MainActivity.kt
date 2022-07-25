package com.example.walksolo

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.walksolo.apihandlers.GoogleVisionAPIHandler
import com.example.walksolo.apihandlers.VisionsResponseHandler
import com.example.walksolo.settings.SettingsActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.util.*

//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothAdapter

class MainActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    private lateinit var toolbar: Toolbar
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var notifyMeButton: Button
    private lateinit var findDeviceButton: Button
    private lateinit var layout: View
    private lateinit var navigateIntent: Intent
    private var connected: Boolean = false
    private var mBluetoothService: BluetoothService? = null
    private var walkingWithMe: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences

    //    private var mImageSaver: ImageSaver = ImageSaver(this, 0)
    private var tts: TextToSpeech? = null


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
//                            status.text = "Connected"
                            connected = true
                        }
                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    //"""val writeMessage = String(writeBuf)"""
                    showErrorMessage("message sent")
                    showErrorMessage(String(writeBuf))
                }
                //Constants.MESSAGE_READ -> {
                // Permission to access the storage is missing. Show rationale and request permission
                //  val readBuf = msg.obj as ByteArray
//                    val path = mImageSaver.saveImage(readBuf)
                // construct a string from the valid bytes in the buffer
                //  callVisionAPI(readBuf, false)
                //    }
                Constants.MESSAGE_READ_ONCE -> {
                    // Permission to access the storage is missing. Show rationale and request permission
                    val readBuf = msg.obj as ByteArray
//                    val path = mImageSaver.saveImage(readBuf)
                    // construct a string from the valid bytes in the buffer
                    callVisionAPI(readBuf, false)
                }
                Constants.MESSAGE_READ_CONSTANT -> {

                    // Permission to access the storage is missing. Show rationale and request permission
                    val readBuf = msg.obj as ByteArray
//                    val path = mImageSaver.saveImage(readBuf)
                    // construct a string from the valid bytes in the buffer
                    callVisionAPI(readBuf, true)
                }
                Constants.MESSAGE_BRANCHES -> {
                    tts!!.speak("branch ahead", TextToSpeech.QUEUE_FLUSH, null, "")
                }
                Constants.MESSAGE_TOAST -> {
//                    status.text = "not_connected"
                    connected = false
                }
            }
        }
    }

    // constant boolean: true if walk with me is on, false if one time request (around me)
    fun callVisionAPI(imageArray: ByteArray, constant: Boolean) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val response = GoogleVisionAPIHandler().detectLocalizedObjects(imageArray)
        val result = VisionsResponseHandler().processResponse(response, constant)
        if (result != "No blockade") {
            notifyHazard(result, constant)
            // maybe change to TextToSpeech.QUEUE_ADD
            tts!!.speak(result, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navigateButton = findViewById(R.id.navigate)
        navigateButton.setOnClickListener(this)
        aroundMeButton = findViewById(R.id.aroundme)
        aroundMeButton.setOnClickListener(this)
        notifyMeButton = findViewById(R.id.notify_me)
        notifyMeButton.setOnClickListener(this)
        findDeviceButton = findViewById(R.id.find_device)
        findDeviceButton.setOnClickListener(this)
        layout = findViewById(R.id.coordinatorLayout)
        mBluetoothService = BluetoothService(handler)
        enableBluetooth()
        if (bluetoothIsEnabled) {
            checkDeviceList()
            if (pairedRaspberryPi != null) {
                mBluetoothService?.connect(pairedRaspberryPi)

            }
        }
        // TextToSpeech(Context: this, OnInitListener: this)
        tts = TextToSpeech(this, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity().javaClass))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // Required func for initiating text To speech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                showErrorMessage("TTS - The Language not supported!")
            }
        } else {
            showErrorMessage("TTS - Initilization Failed!")
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
                var distance_threshold= sharedPreferences.getString("distance_threshold", "150")
                val request = "1," + distance_threshold
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
//                            status.text = "In if State not connected"
                            showErrorMessage("Not Connected")
                        }
                        //val send = "1".toByteArray()
                        val send = request.toByteArray()
                        mBluetoothService?.write(send)

                        // TODO check if already connected
                    }
                }
            }
            R.id.notify_me -> {
                // loop

                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
//                            status.text = "In if 2 State not connected"
                            showErrorMessage("Not Connected")
                        }
                        if (!walkingWithMe) {
                            walkingWithMe = true
                            notifyMeButton.text = "Stop Walking"
                            notifyMeButton.contentDescription = "Stop Walking"
                            var alert_frequency = sharedPreferences.getString("hazard_frequency", "5")
                            var distance_threshold= sharedPreferences.getString("distance_threshold", "150")
                            val request = "2," + alert_frequency+ "," + distance_threshold
                            val send = request.toByteArray()
                            mBluetoothService?.write(send)

                            // TODO check if already connected
                        } else {
                            walkingWithMe = false
                            notifyMeButton.text = "Walk With Me"
                            notifyMeButton.contentDescription = "Walk With Me"
                            val send = "4".toByteArray()
                            mBluetoothService?.write(send)
                        }
                    }
                }
            }
            R.id.find_device -> {
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
                            showErrorMessage("Not Connected")
                        }
                        var buzzer_timeout = sharedPreferences.getString("buzzer_timeout", "3")
                        val request = "3," + buzzer_timeout
                        //val send = "3".toByteArray()
                        val send = request.toByteArray()
                        mBluetoothService?.write(send)

                        // TODO check if already connected

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

    //function for showing the incoming hazard message
    @SuppressLint("ShowToast")
    fun notifyHazard(s: String, constant: Boolean) {
        var message: String = ""
        message = if (constant) {
            "Caution: $s ahead of you!"
        } else {
            "Here's what's around you: $s"
        }
        Snackbar.make(
            layout, message, Snackbar.LENGTH_SHORT
        )
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setActionTextColor(Color.WHITE)
            .setBackgroundTint(Color.BLACK).show()
    }
}
