package com.example.walksolo.apihandlers

import com.example.walksolo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

/** Class to make the calls to the Google Directions API */
class GoogleDirectionsAPIHandler {

    /** Main function that is reachable outside of the class, creates the client and calls to
     * mapsHttpRequest */
    fun getDirections(origin: String, destination: String): String? {
        // Create OkHttp Client
        val client = OkHttpClient()
        return mapsHttpRequest(getRequestUrl(origin, destination), client)
    }

    /** Function to create and send the HTTP request */
    private fun mapsHttpRequest(sUrl: String, client: OkHttpClient): String? {
        var result: String? = null
        try {
            // Create URL
            val url = URL(sUrl)
            // Build request
            val request = Request.Builder().url(url).build()
            // Execute request
            val response = client.newCall(request).execute()
            result = response.body?.string()
        } catch (err: Error) {
            print("Error when executing get request: " + err.localizedMessage)
        }
        return result
    }

    /** Function to create the request url */
    private fun getRequestUrl(origin: String, destination: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&mode=walking&key=${BuildConfig.MAPS_API_KEY}"
    }
}
