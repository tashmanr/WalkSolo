package com.example.walksolo

import android.annotation.SuppressLint
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class MessageReporter(private val layout: View, private var tts: TextToSpeech) {

    //function for showing a message banner at the bottom of the screen
    @SuppressLint("ShowToast")
    fun showMessageBanner(s: String, color: Int = Color.GRAY) {
        Snackbar.make(layout, s, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(color).show()
    }

    fun report(
        s: String,
        isHazard: Boolean = false,
        constant: Boolean = false,
        flush: Boolean = false
    ) {
        var message = s
        if (isHazard) {
            message = if (constant) {
                "Caution: $s ahead of you!"
            } else {
                "Here's what's around you: $s"
            }
        }
        announceMessage(message, flush)
    }

    private fun announceMessage(message: String, flush: Boolean = false) {
        if (flush) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
            return
        }
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, "")

    }
}