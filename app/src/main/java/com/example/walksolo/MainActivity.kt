package com.example.walksolo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.walksolo.apihandlers.DirectionsHandler
import com.example.walksolo.apihandlers.VisionHandler
import com.example.walksolo.permissions.PermissionUtils
import com.example.walksolo.settings.SettingsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
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
    private lateinit var currentLocation: Location
    private var destination: String = ""
    private lateinit var destinationDialog: DestinationDialog
    private var tts: TextToSpeech? = null
    private lateinit var locationManager: LocationManager
    private var locationRequest: LocationRequest? = null
    private lateinit var locationByGps: Location
    private lateinit var locationByNetwork: Location
    private var locationPermissionDenied: Boolean = false
    private var locationEnabled: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var messageReporter: MessageReporter

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        var pairedRaspberryPi: BluetoothDevice? = null
        var m_bluetoothAdapter: BluetoothAdapter? = null
        lateinit var m_pairedDevices: Set<BluetoothDevice>
        var bluetoothIsEnabled: Boolean = false
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private var hasGps: Boolean = false
        private var hasNetwork: Boolean = false
    }

    private val handler = object : Handler(Looper.getMainLooper()) {

        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothService.STATE_CONNECTED -> {
                            connected = true
                        }
                    }
                }
                Constants.MESSAGE_READ_ONCE -> {
                    // Permission to access the storage is missing. Show rationale and request permission
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    onImageReceived(readBuf, false)
                }
                Constants.MESSAGE_READ_CONSTANT -> {
                    // Permission to access the storage is missing. Show rationale and request permission
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    onImageReceived(readBuf, true)
                }
                Constants.MESSAGE_BRANCHES -> {
                    messageReporter.report("branch ahead", flush = true)
                }
                Constants.MESSAGE_TOAST -> {
                    connected = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val sharedPreferenceChangeListener =
            OnSharedPreferenceChangeListener { _, key ->
                if (key == "distance_threshold" || key == "hazard_frequency") {
                    if (walkingWithMe) {
                        endLoop()
                        startLoop()
                    }
                }
            }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
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
        tts = TextToSpeech(this, this)
        messageReporter = MessageReporter(layout, tts!!)
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }
                else -> {
                    // No location access granted.
                }
            }
        }
        // ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
                messageReporter.showMessageBanner("TTS - The Language not supported!")
            }
        } else {
            messageReporter.showMessageBanner("TTS - Initialization Failed!")
        }
    }

    private fun startLoop() {
        walkingWithMe = true
        notifyMeButton.text = getString(R.string.stop_walking)
        notifyMeButton.contentDescription = getString(R.string.stop_walking)
        val alertFrequency =
            sharedPreferences.getString("hazard_frequency", "5")
        val distanceThreshold =
            sharedPreferences.getString("distance_threshold", "150")
        val branchDistance = sharedPreferences.getString("branch_distance_threshold","100")
        val request = "2,$alertFrequency,$distanceThreshold,$branchDistance"
        val send = request.toByteArray()
        mBluetoothService?.write(send)
        // TODO check if already connected
    }

    private fun endLoop() {
        walkingWithMe = false
        notifyMeButton.text = getString(R.string.walk_with_me)
        notifyMeButton.contentDescription = getString(R.string.walk_with_me)
        val send = "4".toByteArray()
        mBluetoothService?.write(send)
    }

    // constant boolean: true if walk with me is on, false if one time request (around me)
    fun onImageReceived(imageArray: ByteArray, constant: Boolean) {
        val result = VisionHandler().callVisionAPI(imageArray, constant)
        if (result != "No blockade") {
            messageReporter.report(result, true, constant)

        }
    }

    private fun onNavigate() {
        val result =
            DirectionsHandler().callDirectionsAPI(locationEnabled, currentLocation, destination)
        messageReporter.report(result)
    }

    //function that waits for a button to be pressed when pressed will execute the following code
    @SuppressLint("MissingPermission")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.navigate -> {
                if (!locationEnabled) {
                    // Create the location request to start receiving updates
                    locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    // Create LocationSettingsRequest object using location request
                    val builder = LocationSettingsRequest.Builder()
                    locationRequest?.let { builder.addLocationRequest(it) }
                    val locationSettingsRequest = builder.build()
                    // Check whether location settings are satisfied
                    val settingsClient = LocationServices.getSettingsClient(this)
                    settingsClient.checkLocationSettings(locationSettingsRequest)
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                    enableLocation()
                }
                getLocation()
                if (destination == "") {
                    openDestinationDialog()
                } else {
                    onNavigate()
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
                            messageReporter.report("Bluetooth Not Connected")
                        }
                        val send = request.toByteArray()
                        mBluetoothService?.write(send)

                    }
                }
            }
            R.id.notify_me -> {
                // loop to get notifications untill stopped by user
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
                            messageReporter.report("Bluetooth Not Connected")
                            return
                        }
                        if (!walkingWithMe) {
                            startLoop()
                        } else {
                            endLoop()
                        }
                    }
                }
            }
            R.id.find_device -> {
                if (bluetoothIsEnabled) {
                    checkDeviceList()
                    if (pairedRaspberryPi != null) {
                        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
                            messageReporter.report("Bluetooth Not Connected")
                        }
                        val buzzerTimeout = sharedPreferences.getString("buzzer_timeout", "20")
                        val request = "3,$buzzerTimeout"
                        val send = request.toByteArray()
                        mBluetoothService?.write(send)

                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (m_bluetoothAdapter == null) {
            messageReporter.report("this device doesn't support bluetooth")
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
        if (m_pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                if (device.name.equals("raspberrypi", ignoreCase = true)) {
                    pairedRaspberryPi = device
                    break
                }
            }
        } else {
            messageReporter.report("no paired devices found")
            return
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            locationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, false
            )
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            )
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(
            this
        ) { location -> // Got last known location. In some rare situations this can be null.
            if (location != null) {
                // Logic to handle location object
                messageReporter.report("Error: Please ensure location permissions are enabled")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let { locationByGps = lastKnownLocationByGps }
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let { locationByNetwork = lastKnownLocationByNetwork }
//------------------------------------------------------//
        currentLocation = if (locationByGps.accuracy > locationByNetwork.accuracy) {
            locationByGps
        } else {
            locationByNetwork
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            locationPermissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (locationPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            locationPermissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(false)
            .show(supportFragmentManager, "dialog")
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    bluetoothIsEnabled = true
                    messageReporter.report("Bluetooth has been enabled")
                } else {
                    messageReporter.report("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                messageReporter.report("Bluetooth enabling has been canceled")
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (locationPermissionDenied) {
                    messageReporter.report("Please enable location permissions")
                    locationPermissionDenied = false

                } else {
                    messageReporter.report("Location permissions have been enabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                messageReporter.report("Enabling location permission has been canceled")
            }
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        val tmpDest = destinationDialog.getDestination()
        if (destination == tmpDest){
            return
        }
        destination = tmpDest
        onNavigate()
    }

    private fun openDestinationDialog() {
        destinationDialog = DestinationDialog()
        destinationDialog.show(supportFragmentManager, "Destination Dialog")
    }

}
