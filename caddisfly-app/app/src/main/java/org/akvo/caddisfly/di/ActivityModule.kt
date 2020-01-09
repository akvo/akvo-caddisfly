package org.akvo.caddisfly.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity

@Suppress("unused")
@Module
interface ActivityModule {
    @ContributesAndroidInjector
    fun contributesMainActivity(): MainActivity

    @ContributesAndroidInjector
    fun contributesTestActivity(): TestActivity
}
