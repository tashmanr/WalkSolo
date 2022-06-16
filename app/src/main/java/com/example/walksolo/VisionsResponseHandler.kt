package com.example.walksolo

import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import java.util.*

class VisionsResponseHandler {
    fun processResponse(responses: BatchAnnotateImagesResponse): String {
        val responseList:ArrayList<Objects> = responses["responses"] as ArrayList<Objects>
        print(responseList[0])
        val annotateImagesResponse = responseList[0] as AnnotateImageResponse
        val description = annotateImagesResponse.labelAnnotations[0].toString()
        return "Caution: $description ahead of you!"
    }
}