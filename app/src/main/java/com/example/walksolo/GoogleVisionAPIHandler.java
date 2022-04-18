package com.example.walksolo;

// Imports the Google Cloud client library

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//import com.google.api.client.googleapis.json.GoogleJsonResponseException;


public class GoogleVisionAPIHandler {

    public static void trial(String... args) throws Exception {
//        ImageView mImageView = null;
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        String fileName = "./image.jpg";
//        File file = new File(fileName);
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//        callCloudVision(bitmap);



//        mImageView.setImageBitmap(bitmap);
//
//        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
//
        // The path to the image file to annotate
//            String fileName = "../../../../../../../image.jpg";
//
        // Reads the image file into memory
//            Path path = Paths.get(fileName);
//            byte[] data = Files.readAllBytes(path);
//            ByteString imgBytes = ByteString.copyFrom(data);
//
//            // Builds the image annotation request
//            List<AnnotateImageRequest> requests = new ArrayList<>();
//            Image img = Image.newBuilder().setContent(imgBytes).build();
//            Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
//            AnnotateImageRequest request =
//                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
//            requests.add(request);
//
//            // Performs label detection on the image file
//            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
//            List<AnnotateImageResponse> responses = response.getResponsesList();
//
//            for (AnnotateImageResponse res : responses) {
//                if (res.hasError()) {
//                    System.out.format("Error: %s%n", res.getError().getMessage());
//                    return;
//                }
//
//                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
//                    annotation
//                            .getAllFields()
//                            .forEach((k, v) -> System.out.format("%s : %s%n", k, v.toString()));
//                }
//            }
//        }
//    }
    }
}

