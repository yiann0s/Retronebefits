package com.yiann0s.retronebefits

import android.app.Application

class App : Application() {

    companion object {
        const val HAS_DELAY = false;
    }

    override fun onCreate() {
        super.onCreate()
    }
}