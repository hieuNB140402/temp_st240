package com.meskiep.vaithat

import android.app.Application
import com.meskiep.vaithat.core.helper.SharePreferenceHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    val sharePreference by lazy { SharePreferenceHelper(this) }

    companion object {
        lateinit var instant: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instant = this
    }

}