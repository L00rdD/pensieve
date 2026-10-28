package com.l0rdd.pensieve

import android.app.Application
import com.l0rdd.pensieve.dependency_injector.AppScope
import com.l0rdd.pensieve.dependency_injector.injectAppScope

/** The application, holding the app-wide scope the injectors build on. */
class PensieveApplication : Application() {
    lateinit var scope: AppScope
        private set

    override fun onCreate() {
        super.onCreate()
        scope = injectAppScope(this)
    }
}
