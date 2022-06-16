package com.example.walksolo

import com.google.api.client.util.ArrayMap
import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import java.util.*

class VisionsResponseHandler {
    fun processResponse(responses: BatchAnnotateImagesResponse): String {
        val responseList: ArrayList<Objects> = responses["responses"] as ArrayList<Objects>
//        print(responses.toPrettyString())
        val annotateImagesResponse = responseList[0] as AnnotateImageResponse
        val objectAnnotations = annotateImagesResponse["localizedObjectAnnotations"]
        //TODO: for now return first one, later add logic to get biggest blockade
        return ((objectAnnotations as List<*>).first() as ArrayMap<*, *>)["name"].toString()
    }
}