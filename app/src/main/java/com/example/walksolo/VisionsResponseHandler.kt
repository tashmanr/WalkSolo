package com.example.walksolo

import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import org.json.JSONObject
import java.util.*

class VisionsResponseHandler {
    fun processResponse(responses: BatchAnnotateImagesResponse): String {
        val responseList: ArrayList<Objects> = responses["responses"] as ArrayList<Objects>
        val annotateImagesResponse = responseList[0] as AnnotateImageResponse
        return JSONObject(annotateImagesResponse.labelAnnotations[0].toString()).getString("description")
    }
}