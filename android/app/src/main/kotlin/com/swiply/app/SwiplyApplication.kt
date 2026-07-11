package com.swiply.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SwiplyApplication : Application() {

    @Inject
    lateinit var appBootstrap: AppBootstrap

    override fun onCreate() {
        super.onCreate()
        appBootstrap.start()
    }
}
