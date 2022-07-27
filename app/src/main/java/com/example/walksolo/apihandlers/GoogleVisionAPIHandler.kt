package com.example.walksolo.apihandlers

import android.util.Base64
import com.example.walksolo.BuildConfig.MAPS_API_KEY
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.IOException

/** Class to make the calls to the Google Vision API */
class GoogleVisionAPIHandler {

    /** Function to create and send the request to the API */
    fun detectLocalizedObjects(imageArray: ByteArray): BatchAnnotateImagesResponse {
        try {
            // create vision builder
            val visionBuilder = Vision.Builder(
                NetHttpTransport(),
                AndroidJsonFactory(),
                null
            ).setApplicationName("WalkSolo")
            visionBuilder.setVisionRequestInitializer(
                VisionRequestInitializer()
            )
            val vision = visionBuilder.build()
            // create input image
            val inputImage = Image().setContent(Base64.encodeToString(imageArray, Base64.DEFAULT))
            // configure feature type and list
            val desiredFeature = Feature().setType("OBJECT_LOCALIZATION").setMaxResults(100)
            val featuresList: ArrayList<Feature> = ArrayList()
            featuresList.add(desiredFeature)
            // create request
            val request = AnnotateImageRequest().setImage(inputImage).setFeatures(featuresList)
            val batchRequest = BatchAnnotateImagesRequest().setRequests(listOf(request))
            val annotate: Vision.Images.Annotate =
                vision.images().annotate(batchRequest).setKey(MAPS_API_KEY)
            return annotate.execute()
        } catch (e: IOException) {
            println("Image annotation failed:")
            println(e.message)
            return BatchAnnotateImagesResponse()
        }
    }
}

