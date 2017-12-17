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

package org.akvo.caddisfly.ui;

import android.support.annotation.StringRes;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class AppPreferencesTest {

    private static final boolean DEFAULT_FALSE = false;

    @Test
    public void checkPlaySound() {

        String methodName = "isSoundOn";

        checkDiagnosticPreference(R.string.soundOnKey, true, methodName, false);

        Method method;
        try {
            method = AppPreferences.class.getDeclaredMethod(methodName);

            try {
                assertEquals(true, method.invoke(null));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkDebugMessagesKey() {

        String methodName = "getShowDebugMessages";

        checkDiagnosticPreference(R.string.showDebugMessagesKey, DEFAULT_FALSE, methodName, true);

        Method method;
        try {
            method = AppPreferences.class.getDeclaredMethod(methodName);

            try {
                assertEquals(DEFAULT_FALSE, method.invoke(null));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkSamplingTimes() {
        assertEquals(7, AppPreferences.getSamplingTimes());
    }

    private void checkDiagnosticPreference(@StringRes int key, Object defaultValue,
                                           String methodName, Object newValue) {
        Method method;
        try {
            method = AppPreferences.class.getDeclaredMethod(methodName);

            assertEquals(defaultValue, method.invoke(null));

            AppPreferences.enableDiagnosticMode();
            assertEquals(defaultValue, method.invoke(null));

            if (defaultValue instanceof Boolean) {
                PreferencesUtil.setBoolean(RuntimeEnvironment.application, key, !(boolean) defaultValue);
                assertEquals(!(boolean) defaultValue, method.invoke(null));
            } else if (defaultValue instanceof Integer) {
                PreferencesUtil.setString(RuntimeEnvironment.application, key, newValue.toString());
                assertEquals(newValue, method.invoke(null));
            }

            AppPreferences.disableDiagnosticMode();
            assertEquals(defaultValue, method.invoke(null));
        } catch (NoSuchMethodException e) {
            assertEquals("Error in method call, check method name", "<correctMethodName>", methodName);
        } catch (Exception e) {
            assertEquals("Unknown error", 1, 0);
        }
    }
}
