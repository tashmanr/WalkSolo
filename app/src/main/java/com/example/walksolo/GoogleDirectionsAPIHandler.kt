package com.example.walksolo

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL


class GoogleDirectionsAPIHandler {
    fun getDirections(origin: String, destination: String): String? {
        // Create OkHttp Client
        val client = OkHttpClient()
        return mapsHttpRequest(getRequestUrl(origin, destination), client)
    }

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

    private fun getRequestUrl(origin: String, destination: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&mode=walking&key=${BuildConfig.MAPS_API_KEY}"
    }
}
