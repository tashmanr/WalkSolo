package com.example.walksolo

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment


class DestinationDialog() : DialogFragment() {
    // Use this instance of the interface to deliver action events
    internal lateinit var listener: DestinationDialogListener
    private lateinit var destination: String

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DestinationDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the DestinationDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DestinationDialogListener so we can send events to the host
            listener = context as DestinationDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement DestinationDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(activity)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val viewInflated = inflater.inflate(R.layout.dialog_destination, null)
            val input = viewInflated.findViewById<EditText>(R.id.destination)
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(viewInflated)
            builder.setTitle("Destination Dialog")
            builder.setMessage("Where would you like to go?")
            builder.setPositiveButton(
                "Start",
                DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    destination = input.text.toString();
                    listener.onDialogPositiveClick(this)
                }
            )
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    fun getDestination(): String {
        return destination
    }
}