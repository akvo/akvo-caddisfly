package org.akvo.caddisfly.di

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.tasks.TaskExecutors
import dagger.Module
import dagger.Provides
import org.akvo.caddisfly.app.CaddisflyApp
import java.util.concurrent.Executor

@Module
object AppModule {
    @Provides
    fun providesInAppUpdateManager(application: CaddisflyApp): AppUpdateManager {
        return AppUpdateManagerFactory.create(application)
    }

    @Provides
    fun providesPlayServiceExecutor(): Executor {
        return TaskExecutors.MAIN_THREAD
    }
}
