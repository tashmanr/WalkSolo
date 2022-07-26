package com.example.walksolo.apihandlers

import android.text.Html
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DirectionsResponseHandler {
    fun processResponse(response: String?): String {
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
                nextStep =
                    Html.fromHtml(steps.getJSONObject(counter).getString("html_instructions"))
                        .toString()
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
}