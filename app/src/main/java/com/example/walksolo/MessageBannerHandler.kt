package com.example.walksolo

import android.annotation.SuppressLint
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class MessageBannerHandler(private val layout: View, private var tts: TextToSpeech) {

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

    fun announceMessage(message: String, flush: Boolean = false) {
        if (flush) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
            return
        }
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, "")

    }
}