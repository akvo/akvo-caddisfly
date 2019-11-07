package org.akvo.caddisfly.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.akvo.caddisfly.app.CaddisflyApp
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            ActivityModule::class,
            AppModule::class
        ]
)
interface AppComponent : AndroidInjector<CaddisflyApp> {
    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<CaddisflyApp>
}
