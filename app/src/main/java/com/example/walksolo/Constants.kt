package com.example.walksolo

class Constants {

    companion object {

        // Message types sent from the BluetoothChatService Handler
        var MESSAGE_TYPE_SENT = 0
        val MESSAGE_STATE_CHANGE = 1
        var MESSAGE_TYPE_RECEIVED = 1
        val MESSAGE_READ = 2
        val MESSAGE_WRITE = 3
        val MESSAGE_DEVICE_NAME = 4
        val MESSAGE_TOAST = 5
        val MESSAGE_RECEIVED: Int = 6
        val MESSAGE_BRANCHES: Int = 7


        const val STATE_CONNECTING: Int = 7
        const val STATE_CONNECTON_FAILED: Int = 8
        const val STATE_CONNECTED: Int = 9
        const val STATE_CANNOT_WRITE = 10

        // Key names received from the BluetoothChatService Handler
        val DEVICE_NAME = "device_name"
        val TOAST = "toast"
    }

}