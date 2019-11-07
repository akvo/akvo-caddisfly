package org.akvo.caddisfly.update.di

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import dagger.Module
import dagger.Provides
import org.akvo.caddisfly.app.CaddisflyApp
import java.util.concurrent.Executor
import javax.inject.Singleton

// https://github.com/malvinstn/FakeAppUpdateManagerSample
@Module
object TestAppModule {

    @Singleton
    @Provides
    fun providesFakeInAppUpdateManager(application: CaddisflyApp): FakeAppUpdateManager {
        return FakeAppUpdateManager(application)
    }

    @Provides
    fun providesInAppUpdateManager(fakeAppUpdateManager: FakeAppUpdateManager): AppUpdateManager {
        return fakeAppUpdateManager
    }

    @Provides
    fun providesPlayServiceExecutor(): Executor {
        return Executor { it.run() }
    }
}