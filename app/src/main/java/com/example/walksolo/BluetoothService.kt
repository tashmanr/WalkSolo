package com.example.walksolo

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothService(handler: Handler) {

    // Member fields
    private var mAdapter: BluetoothAdapter? = null
    private var mHandler: Handler? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int = 0
    private var mNewState: Int = 0

    private val  TAG: String = javaClass.simpleName

    // Unique UUID for this application
    var mUUID: UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

    // Constants that indicate the current connection state
    companion object {
        val STATE_NONE = 0       // we're doing nothing
        val STATE_LISTEN = 1     // now listening for incoming connections
        val STATE_CONNECTING = 2 // now initiating an outgoing connection
        val STATE_CONNECTED = 3  // now connected to a remote device
    }

    init {

        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
        mNewState = mState
        mHandler = handler
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    @Synchronized private fun updateStatusTitle() {
        mState = getState()
        Log.d(TAG, "updateStatusTitle() $mNewState -> $mState")
        mNewState = mState

        // Give the new state to the Handler so the UI Activity can update
        mHandler!!.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget()
    }

    /**
     * Return the current connection state.
     */
    @Synchronized fun getState(): Int {
        return mState
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device The BluetoothDevice to connect
     */
    @Synchronized fun connect(device: BluetoothDevice?) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread?.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
        updateStatusTitle()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket The BluetoothSocket on which the connection was made
     * *
     * @param device The BluetoothDevice that has been connected
     */
    @SuppressLint("MissingPermission")
    @Synchronized fun connected(socket: BluetoothSocket?, device: BluetoothDevice?) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device?.name)
        msg?.data = bundle
        if (msg != null) {
            mHandler?.sendMessage(msg)
        }
        updateStatusTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized fun stop() {
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        mState = STATE_NONE
        updateStatusTitle()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * *
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Create temporary object
        var r: ConnectedThread?  = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r?.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Unable to connect device")
        msg?.data = bundle
        if (msg != null) {
            mHandler?.sendMessage(msg)
        }

        mState = STATE_NONE

        // Start the service over to restart listening mode
        //this@BluetoothService.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Device connection was lost")
        msg?.data = bundle
        if (msg != null) {
            mHandler?.sendMessage(msg)
        }

        mState = STATE_NONE

        // Start the service over to restart listening mode
        //this@BluetoothService.start()
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val mmDevice: BluetoothDevice?) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mmDevice?.createRfcommSocketToServiceRecord(mUUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: create() failed", e)
            }

            mmSocket = tmp
            mState = STATE_CONNECTING
        }

        override fun run() {

            Log.i(TAG, "BEGIN mConnectThread")
            name = "ConnectThread"

            // Always cancel discovery because it will slow down a connection
            mAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket?.connect()

            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket?.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) {
                mConnectThread = null
            }

            // Start the connected thread
            connected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {

        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread ")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket?.inputStream
                tmpOut = mmSocket?.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            var buffer: ByteArray = ByteArray(0)
            var bytes: Int
            var newMsg: Boolean = true
            var bytesToReceive: Int = 0
            var bytesReceived: Int = 0
            var flagMsgType: Int = 0
            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try{
                    if(newMsg){
                        if(mmInStream!!.available() > 5)
                        {
                            try{
                                // Read from the InputStream.
                                var data = ByteArray(8)
                                val received: Int = mmInStream.read(data)
                                buffer = ByteArray(0)
                                var response:String = String(data)
                                if(response == "0,branch"){
                                    mHandler?.obtainMessage(Constants.MESSAGE_BRANCHES, 1, -1, buffer)
                                        ?.sendToTarget()
                                }
                                else{
                                    if(response.get(0).toString() == "1"){
                                        flagMsgType = 1
                                    }
                                    else if(response.get(0).toString() == "2"){
                                        flagMsgType = 2
                                    }
                                    bytesToReceive = response.substring(2).toInt()
                                    bytesReceived = 0
                                    newMsg = false
                                }
                            }
                            catch (e: IOException) {
                                // no new msg yet
                            }
                        }
                    }
                    else{
                        var data = ByteArray(mmInStream!!.available())
                        val received: Int = mmInStream.read(data)

                        buffer += data
                        System.arraycopy(data, 0, buffer, bytesReceived, received)
                        bytesReceived += received

                        if (bytesReceived == bytesToReceive) {
                            if(flagMsgType == 1){
                                mHandler?.obtainMessage(Constants.MESSAGE_READ_ONCE, 1, -1, buffer)
                                    ?.sendToTarget()
                            }
                            else if(flagMsgType == 2){
                                mHandler?.obtainMessage(Constants.MESSAGE_READ_CONSTANT, 1, -1, buffer)
                                    ?.sendToTarget()
                            }

                            newMsg = true
                            flagMsgType = 0
                        }
                    }
                }catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                mmOutStream?.write(buffer)

                // Share the sent message back to the UI Activity
                //mHandler?.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                //    ?.sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

}