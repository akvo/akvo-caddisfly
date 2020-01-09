package org.akvo.caddisfly.update.di

import androidx.test.platform.app.InstrumentationRegistry
import org.akvo.caddisfly.app.CaddisflyApp

object TestInjector {
    fun inject(): TestAppComponent {
        val application = InstrumentationRegistry.getInstrumentation()
                .targetContext.applicationContext as CaddisflyApp
        return DaggerTestAppComponent.factory()
                .create(application)
                .also { it.inject(application) } as TestAppComponent
    }
}
