//package com.example.walksolo;
//
////import com.example.walksolo.BuildConfig.GOOGLE_APPLICATION_CREDENTIALS
//import com.google.api.gax.core.FixedCredentialsProvider
//import com.google.cloud.vision.v1.*
//import com.google.protobuf.ByteString
//import java.io.FileInputStream
//import java.io.IOException
//
//
//class Trial {
//    private lateinit var requests: List<AnnotateImageRequest>
//    private lateinit var imgBytes: ByteString
//    private lateinit var img: Image
//    private lateinit var request: AnnotateImageRequest
//    private lateinit var client: ImageAnnotatorClient
//    private lateinit var response: BatchAnnotateImagesResponse
//    private lateinit var responses: List<AnnotateImageResponse>
//    private lateinit var mImageAnnotatorSettings: ImageAnnotatorSettings
//
//
//    /**
//     * Detects localized objects in the specified local image.
//     *
//     * @param filePath The path to the file to perform localized object detection on.
//     * @throws Exception on errors while closing the client.
//     * @throws IOException on Input/Output errors.
//     */
//    fun detectLocalizedObjects(filePath: String) {
//        requests = ArrayList()
//        imgBytes = ByteString.readFrom(FileInputStream(filePath))
//        img = Image.newBuilder().setContent(imgBytes).build()
//        request =
//            AnnotateImageRequest.newBuilder()
//                .addFeatures(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION))
//                .setImage(img)
//                .build()
//        (requests as ArrayList<AnnotateImageRequest>).add(request)
//
//        // Initialize client that will be used to send requests. This client only needs to be created
//        // once, and can be reused for multiple requests. After completing all of your requests, call
//        // the "close" method on the client to safely clean up any remaining background resources.
//        // Perform the request
//        try {
////            val credentialsFileName = "Users/rebecca/Desktop/WalkSolo/walksolo-64ae3d216611.json"
////            val myCredentials: Credentials = GoogleCredentialsProvider
////                .fromStream(
////                FileInputStream(credentialsFileName)
////            )
//            mImageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
//                .setCredentialsProvider(FixedCredentialsProvider.create())
//                .build()
//
//            client = ImageAnnotatorClient.create(mImageAnnotatorSettings)
//            response = client.batchAnnotateImages(requests)
//            responses = response.responsesList
//            // Display the results
//            for (res in responses) {
//                for (entity in res.localizedObjectAnnotationsList) {
//                    System.out.format("Object name: %s%n", entity.name)
//                    System.out.format("Confidence: %s%n", entity.score)
////                    System.out.format("Normalized Vertices:%n")
////                    entity
////                        .boundingPoly
////                        .normalizedVerticesList
////                            .forEach(vertex -> System.out.format("- (%s, %s)%n", vertex.getX(), vertex.getY()));
//                }
//            }
////        }
//        } catch (e: IOException) {
//            println(e)
//        }
//    }
//}
