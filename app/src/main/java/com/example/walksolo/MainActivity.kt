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
import androidx.fragment.app.DialogFragment
import com.example.walksolo.apihandlers.DirectionsResponseHandler
import com.example.walksolo.apihandlers.GoogleDirectionsAPIHandler
import com.example.walksolo.apihandlers.GoogleVisionAPIHandler
import com.example.walksolo.apihandlers.VisionsResponseHandler
import com.example.walksolo.settings.SettingsActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity(), DestinationDialog.DestinationDialogListener,
    View.OnClickListener, TextToSpeech.OnInitListener {
    private lateinit var toolbar: Toolbar
    private lateinit var navigateButton: Button
    private lateinit var aroundMeButton: Button
    private lateinit var notifyMeButton: Button
    private lateinit var findDeviceButton: Button
    private lateinit var setDestinationButton: Button
    private lateinit var layout: View
    private var connected: Boolean = false
    private var mBluetoothService: BluetoothService? = null
    private var walkingWithMe: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private var destination: String = ""
    private lateinit var destinationDialog: DestinationDialog
    private var tts: TextToSpeech? = null


    companion object {
        private const val REQUEST_ENABLE_BT = 1
        var pairedRaspberryPi: BluetoothDevice? = null
        var m_bluetoothAdapter: BluetoothAdapter? = null
        lateinit var m_pairedDevices: Set<BluetoothDevice>
        var bluetoothIsEnabled: Boolean = false
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        destination = destinationDialog.getDestination()
        callDirectionsAPI()
    }

    private fun openDestinationDialog() {
        destinationDialog = DestinationDialog()
        destinationDialog.show(supportFragmentManager, "Destination Dialog")

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
                    showMessageBanner("message sent")
                    showMessageBanner(String(writeBuf))
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

    private fun callDirectionsAPI() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val directionsResponse =
            GoogleDirectionsAPIHandler().getDirections(
                "Kaf Gimel 9 Givatayim",
                destination
            )
//            GoogleDirectionsAPIHandler().getDirections(currentLocation?.latitude.toString() + "," + currentLocation?.longitude.toString(), destination)
        val nextStep = DirectionsResponseHandler().processResponse(directionsResponse)
        showMessageBanner(nextStep)
        tts!!.speak(nextStep, TextToSpeech.QUEUE_ADD, null, "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navigateButton = findViewById(R.id.navigate)
        navigateButton.setOnClickListener(this)
        aroundMeButton = findViewById(R.id.around_me)
        aroundMeButton.setOnClickListener(this)
        notifyMeButton = findViewById(R.id.notify_me)
        notifyMeButton.setOnClickListener(this)
        findDeviceButton = findViewById(R.id.find_device)
        findDeviceButton.setOnClickListener(this)
        setDestinationButton = findViewById(R.id.set_destination)
        setDestinationButton.setOnClickListener(this)
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
                showMessageBanner("TTS - The Language not supported!")
            }
        } else {
            showMessageBanner("TTS - Initilization Failed!")
        }
    }

    //function that waits for a button to be pressed when pressed will execute the following code
    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.navigate -> {
                if (destination == "") {
                    openDestinationDialog()
                } else {
                    callDirectionsAPI()
                }
            }
            R.id.set_destination -> {
                openDestinationDialog()
            }
            R.id.around_me -> {
                val distanceThreshold = sharedPreferences.getString("distance_threshold", "150")
                val request = "1,$distanceThreshold"
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
//                            status.text = "In if State not connected"
                            showMessageBanner("Not Connected")
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
                            showMessageBanner("Not Connected")
                            return
                        }
                        if (!walkingWithMe) {
                            walkingWithMe = true
                            notifyMeButton.text = "Stop Walking"
                            notifyMeButton.contentDescription = "Stop Walking"
                            val alertFrequency =
                                sharedPreferences.getString("hazard_frequency", "5")
                            val distanceThreshold =
                                sharedPreferences.getString("distance_threshold", "150")
                            val request = "2,$alertFrequency,$distanceThreshold"
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
                            showMessageBanner("Not Connected")
                        }
                        val buzzerTimeout = sharedPreferences.getString("buzzer_timeout", "3")
                        val request = "3,$buzzerTimeout"
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
            showMessageBanner("this device doesn't support bluetooth")
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
            showMessageBanner("no paired devices found")
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
                    showMessageBanner("Bluetooth has been enabled")
                } else {
                    showMessageBanner("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showMessageBanner("Bluetooth enabling has been canceled")
            }
        }
    }

    //function for showing a message banner at the bottom of the screen
    @SuppressLint("ShowToast")
    fun showMessageBanner(s: String, color: Int = Color.GRAY) {
        Snackbar.make(layout, s, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(color).show()
    }

    //function for showing the incoming hazard message
    @SuppressLint("ShowToast")
    fun notifyHazard(s: String, constant: Boolean) {
        val message = if (constant) {
            "Caution: $s ahead of you!"
        } else {
            "Here's what's around you: $s"
        }
        showMessageBanner(message, Color.BLACK)
    }
}
