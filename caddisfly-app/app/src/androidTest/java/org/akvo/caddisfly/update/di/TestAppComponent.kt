package org.akvo.caddisfly.update.di

import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.di.ActivityModule
import org.akvo.caddisfly.di.AppComponent
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            ActivityModule::class,
            TestAppModule::class
        ]
)
interface TestAppComponent : AppComponent {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<CaddisflyApp>

    fun fakeAppUpdateManager(): FakeAppUpdateManager
}
