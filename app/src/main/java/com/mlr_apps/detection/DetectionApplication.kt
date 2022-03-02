package com.mlr_apps.detection

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class DetectionApplication :Application() {
    override fun onCreate() {
        super.onCreate()
    }
}