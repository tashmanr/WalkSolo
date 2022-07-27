package com.example.walksolo.apihandlers

import android.location.Location
import android.os.StrictMode
import androidx.core.text.HtmlCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Class to handle calls to GoogleDirectionsAPIHandler and process the response received */
class DirectionsHandler {
    /** Function to process the response that was received in order to get the first step in the
     * directions from current location */
    private fun processResponse(response: String?): String {
        var nextStep = ""
        try {
            // get JSONObject from JSON file
            val obj = JSONObject(response!!)
            // fetch JSONArray named routes
            val routes: JSONArray = obj.getJSONArray("routes")
            // get JSONArray named legs
            val legs: JSONArray = routes.getJSONObject(0).getJSONArray("legs")
            // get JSONArray named steps
            val steps: JSONArray = legs.getJSONObject(0).getJSONArray("steps")
            var counter = 0
            // get the next step in the navigation
            while (nextStep == "" && counter < steps.length()) {
                nextStep = HtmlCompat.fromHtml(
                    steps.getJSONObject(counter).getString("html_instructions"),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString()
                counter++
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (nextStep == "") {
            nextStep = "Error, re-input your destination"
        }
        return nextStep
    }

    /** Function to call GoogleDirectionsAPIHandler & return the result once it was processed */
    fun callDirectionsAPI(locationEnabled: Boolean, origin: Location, destination: String): String {
        var nextStep = "Please enable your location to continue"
        if (locationEnabled) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val directionsResponse = GoogleDirectionsAPIHandler().getDirections(
                origin.latitude.toString() + "," + origin.longitude.toString(),
                destination
            )
            nextStep = processResponse(directionsResponse)
        }
        return nextStep
    }
}