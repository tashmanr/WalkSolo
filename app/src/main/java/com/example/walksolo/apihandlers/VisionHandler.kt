package com.example.walksolo.apihandlers

import android.os.StrictMode
import com.google.api.client.util.ArrayMap
import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import java.util.*

/** Class to handle calls to GoogleVisionAPIHandler and process the response received */
class VisionHandler {
    /** Function to process the response that was received */
    private fun processResponse(responses: BatchAnnotateImagesResponse, constant: Boolean): String {
        if (responses["responses"] != null) {
            val responseList: ArrayList<Objects> = responses["responses"] as ArrayList<Objects>
            //if nothing was identified
            if (responseList.size < 1) {
                return "No blockade"
            }
            val annotateImagesResponse = responseList[0] as AnnotateImageResponse
            val objectAnnotations = annotateImagesResponse["localizedObjectAnnotations"]
            if (objectAnnotations != null) {
                //if something was identified
                if ((objectAnnotations as List<*>).size >= 1) {
                    // if constantly getting notified, only send one
                    if (constant) {
                        //TODO: for now return first one, later add logic to get biggest blockade
                        return (objectAnnotations.first() as ArrayMap<*, *>)["name"].toString()
                    }
                    // else if one time request, send all objects that were recognized
                    var result = ""
                    for (o in objectAnnotations) {
                        result += (o as ArrayMap<*, *>)["name"].toString() + " "
                    }
                    return result
                }
            }
        }
        //else nothing was identified
        return "No blockade"
    }

    /** Function to call GoogleVisionAPIHandler & return the result once it was processed */
    // constant boolean: true if walk with me is on, false if one time request (around me)
    fun callVisionAPI(imageArray: ByteArray, constant: Boolean): String {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val response = GoogleVisionAPIHandler().detectLocalizedObjects(imageArray)
        return processResponse(response, constant)
    }
}