package com.example.walksolo

import android.content.Context
import java.io.FileOutputStream

class ImageSaver(private var context: Context, private var imagesSaved: Int) {
    init {
        imagesSaved = 0
    }

    fun saveImage(array: ByteArray):String {

        val fileName = "image" + (++imagesSaved).toString()
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(array)
            return context.filesDir.toString() + '/'+ fileName
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}