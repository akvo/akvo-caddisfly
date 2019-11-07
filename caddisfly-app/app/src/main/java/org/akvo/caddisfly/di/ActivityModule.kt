package org.akvo.caddisfly.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.akvo.caddisfly.ui.MainActivity

@Suppress("unused")
@Module
interface ActivityModule {
    @ContributesAndroidInjector
    fun contributesMainActivity(): MainActivity
}
