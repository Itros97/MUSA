package org.softwareanvil.musa

import android.app.Application
import android.content.pm.ApplicationInfo
import org.koin.android.ext.koin.androidContext
import org.softwareanvil.musa.di.initKoin

/** Starts dependency injection with the Android context the database needs; debug builds get the development tools. */
class MusaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        initKoin(devTools = debuggable) { androidContext(this@MusaApplication) }
    }
}
