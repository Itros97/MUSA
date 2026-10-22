package org.softwareanvil.musa

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.softwareanvil.musa.di.initKoin

/** Starts dependency injection with the Android context the database needs. */
class MusaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@MusaApplication) }
    }
}
