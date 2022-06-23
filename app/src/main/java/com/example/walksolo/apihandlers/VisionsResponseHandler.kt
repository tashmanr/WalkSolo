package com.example.walksolo.apihandlers

import com.google.api.client.util.ArrayMap
import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import java.util.*

class VisionsResponseHandler {
    fun processResponse(responses: BatchAnnotateImagesResponse, constant: Boolean): String {
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
                        return ((objectAnnotations as List<*>).first() as ArrayMap<*, *>)["name"].toString()
                    }
                    // else if one time request, send all objects that were recognized
                    var result: String = ""
                    for (o in (objectAnnotations as List<*>)) {
                        result += (o as ArrayMap<*, *>)["name"].toString() + " "
                    }
                    return result
                }
            }
        }

        //else nothing was identified
        return "No blockade"
    }
}