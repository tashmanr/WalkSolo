package com.example.walksolo.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.walksolo.R

/** SettingsFragment class, inherits PreferenceFragment */
class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }
}