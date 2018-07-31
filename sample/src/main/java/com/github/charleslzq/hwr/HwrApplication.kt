package com.github.charleslzq.hwr

import android.app.Application

class HwrApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        HciHwrEngine.setup(this, "43c5a7be629fc2e7f0c2d792a04c367d", "545d5463")
    }

    override fun onTerminate() {
        super.onTerminate()
        HciHwrEngine.release()
    }
}