package com.example.walksolo

import android.annotation.SuppressLint
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

/** Class for reporting the messages through TextToSpeech */
class MessageReporter(private val layout: View, private var tts: TextToSpeech) {

    /** Function for showing a message banner at the bottom of the screen
     * only called if TTS isn't working */
    @SuppressLint("ShowToast")
    fun showMessageBanner(s: String, color: Int = Color.GRAY) {
        Snackbar.make(layout, s, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(color).show()
    }

    /** Function to pass reports from main activity
     * isHazard boolean -> if message being passed is from image recognition
     * constant boolean -> if hazard is from around_me or walk_with_me
     * flush boolean -> if message is urgent (branches ahead)
     * */
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

    /** Function to announce the message through TTS */
    private fun announceMessage(message: String, flush: Boolean = false) {
        if (flush) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
            return
        }
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, "")

    }
}