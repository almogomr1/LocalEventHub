package com.localeventhub.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocalEventHub : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}