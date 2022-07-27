package com.example.walksolo

// Class of constants for bluetooth use and messages sent over
class Constants {

    companion object {

        // Message types sent from the BluetoothChatService Handler
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val MESSAGE_BRANCHES: Int = 7

        const val MESSAGE_READ_ONCE: Int = 8 //sent one pic

        const val MESSAGE_READ_CONSTANT: Int = 9 //loop

        // Key names received from the BluetoothChatService Handler
        const val DEVICE_NAME = "device_name"
        const val TOAST = "toast"
    }

}