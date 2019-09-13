package org.akvo.caddisfly.util

import android.provider.Settings.System
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume
import org.junit.Before

open class BaseTest {

    @Before
    open fun setUp() {
        assertDeviceOrSkip()
    }

    private fun assertDeviceOrSkip() {
        try {
            if (javaClass.isAnnotationPresent(RequiresExternalApp::class.java)) {

                val testLabSetting: String = System.getString(
                        InstrumentationRegistry.getInstrumentation()
                                .targetContext.contentResolver, "firebase.test.lab")

                Assume.assumeFalse("true" == testLabSetting)
            }
        } catch (e: IllegalStateException) {
        }
    }
}