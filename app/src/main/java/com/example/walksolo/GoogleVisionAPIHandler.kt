package com.example.walksolo

import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.IOException


class GoogleVisionAPIHandler {
    fun detectLocalizedObjects(imageArray: ByteArray) {
        try {
            val visionBuilder = Vision.Builder(
                    NetHttpTransport(),
                    AndroidJsonFactory(),
                    null
            ).setApplicationName("WalkSolo")

            visionBuilder.setVisionRequestInitializer(
                    VisionRequestInitializer()
            )
            val vision = visionBuilder.build()
            val inputImage = Image().setContent(imageArray.toString())
            val desiredFeature = Feature().setType("LABEL_DETECTION")
            val featuresList: ArrayList<Feature> = ArrayList()
            featuresList.add(desiredFeature)
            val request = AnnotateImageRequest().setImage(inputImage).setFeatures(featuresList)
            println(request.toPrettyString())
            val batchRequest = BatchAnnotateImagesRequest().setRequests(listOf(request))
            val annotate: Vision.Images.Annotate = vision.images().annotate(batchRequest)
            val batchResponse: BatchAnnotateImagesResponse = annotate.execute()
//            annotate.setDisableGZipContent(true).execute()
            println("yay2")
            println(batchResponse.toPrettyString())
        } catch (e: IOException) {
            println("Image annotation failed:")
            println(e.message)
        }
    }
}

