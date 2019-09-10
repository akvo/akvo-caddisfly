/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert.assertEquals
import org.akvo.caddisfly.R
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.util.PreferencesUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
class AppPreferencesTest {

    @Test
    fun checkPlaySound() {

        val methodName = "isSoundOn"

        checkDiagnosticPreference(R.string.soundOnKey, true, methodName, false)

        val method: Method
        try {
            method = AppPreferences::class.java.getDeclaredMethod(methodName)

            try {
                assertEquals(true, method.invoke(null))
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }

        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

    }

    @Test
    fun checkDebugMessagesKey() {

        val methodName = "getShowDebugInfo"

        checkDiagnosticPreference(R.string.showDebugMessagesKey, DEFAULT_FALSE, methodName, true)

        val method: Method
        try {
            method = AppPreferences::class.java.getDeclaredMethod(methodName)

            try {
                assertEquals(DEFAULT_FALSE, method.invoke(null))
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }

        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

    }

    private fun checkDiagnosticPreference(@StringRes key: Int, defaultValue: Any,
                                          methodName: String, newValue: Any) {
        val method: Method
        try {
            method = AppPreferences::class.java.getDeclaredMethod(methodName)

            assertEquals(defaultValue, method.invoke(null))

            AppPreferences.enableDiagnosticMode()
            assertEquals(defaultValue, method.invoke(null))

            if (defaultValue is Boolean) {
                PreferencesUtil.setBoolean(ApplicationProvider.getApplicationContext<Context>(), key, !defaultValue)
                assertEquals(!defaultValue, method.invoke(null))
            } else if (defaultValue is Int) {
                PreferencesUtil.setString(ApplicationProvider.getApplicationContext<Context>(), key, newValue.toString())
                assertEquals(newValue, method.invoke(null))
            }

            AppPreferences.disableDiagnosticMode()
            assertEquals(defaultValue, method.invoke(null))
        } catch (e: NoSuchMethodException) {
            assertEquals("Error in method call, check method name", "<correctMethodName>", methodName)
        } catch (e: Exception) {
            assertEquals("Unknown error", 1, 0)
        }

    }

    companion object {

        private val DEFAULT_FALSE = false
    }
}
