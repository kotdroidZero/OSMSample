package com.kotdroid.osm.utils

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration

class Application: Application() {

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }
}