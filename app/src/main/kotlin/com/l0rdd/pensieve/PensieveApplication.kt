package com.l0rdd.pensieve

import android.app.Application
import com.l0rdd.pensieve.dependency_injector.AppScope
import com.l0rdd.pensieve.dependency_injector.injectAppScope
import com.l0rdd.pensieve.presentation.views.common.Palettes

/** The application, holding the app-wide scope the injectors build on. */
class PensieveApplication : Application() {
    lateinit var scope: AppScope
        private set

    override fun onCreate() {
        super.onCreate()
        // the looks' colours, before any view is drawn
        Palettes.load(assets.open("looks.json").bufferedReader().use { it.readText() })
        scope = injectAppScope(this)
    }
}
