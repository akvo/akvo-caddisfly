/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.content.Context;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("unused")
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AppPreferencesTest {

    @Test
    public void checkPlaySound() {
        checkDiagnosticPreference(R.string.noSoundKey, false, "isSoundOff", true);
    }

    @Test
    public void checkIgnoreShake() {
        checkDiagnosticPreference(R.string.ignoreShakeKey, false, "getIgnoreShake", true);
    }

    @Test
    public void checkUseFlashMode() {
        checkDiagnosticPreference(R.string.useFlashModeKey, false, "getUseFlashMode", true);
    }

    @Test
    public void checkAutoFocus() {
        checkDiagnosticPreference(R.string.autoFocusKey, false, "getAutoFocus", true);
    }

    @Test
    public void checkDebugMessagesKey() {
        checkDiagnosticPreference(R.string.showDebugMessagesKey, false, "getShowDebugMessages", true);
    }

    @Test
    public void checkSamplingTimes() {
        checkDiagnosticPreference(R.string.samplingsTimeKey, 5, "getSamplingTimes", 1);
    }

    private void checkDiagnosticPreference(@StringRes int key, Object defaultValue,
                                           String methodName, Object newValue) {
        Method method;
        try {
            method = AppPreferences.class.getDeclaredMethod(methodName, Context.class);

            assertEquals(defaultValue, method.invoke(null, RuntimeEnvironment.application));

            AppPreferences.enableDiagnosticMode(RuntimeEnvironment.application);
            assertEquals(defaultValue, method.invoke(null, RuntimeEnvironment.application));

            if (defaultValue instanceof Boolean) {
                PreferencesUtils.setBoolean(RuntimeEnvironment.application, key, !(boolean) defaultValue);
                assertEquals(!(boolean) defaultValue, method.invoke(null, RuntimeEnvironment.application));
            } else if (defaultValue instanceof Integer) {
                PreferencesUtils.setString(RuntimeEnvironment.application, key, newValue.toString());
                assertEquals(newValue, method.invoke(null, RuntimeEnvironment.application));
            }

            AppPreferences.disableDiagnosticMode(RuntimeEnvironment.application);
            assertEquals(defaultValue, method.invoke(null, RuntimeEnvironment.application));
        } catch (NoSuchMethodException e) {
            assertEquals("Error in method call, check method name", "<correctMethodName>", methodName);
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals("Unknown error", 1, 0);
        }
    }
}