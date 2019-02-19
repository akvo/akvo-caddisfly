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

package org.akvo.caddisfly.misc;

import android.content.Intent;
import android.os.SystemClock;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.TestConstantKeys;
import org.akvo.caddisfly.ui.MainActivity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.akvo.caddisfly.util.TestHelper.currentHashMap;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.object.HasToString.hasToString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LanguageTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

    }

    @Test
    @Ignore
    public void testLanguage() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(false);

        onView(withText(version)).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get(TestConstantKeys.LANGUAGE)))).perform(click());
    }

    @Test
    @Ignore
    public void testLanguageFrench() {
        languageTest("fr");
    }

    @Test
    @Ignore
    public void testLanguageEnglish() {
        languageTest("en");
    }

    private void languageTest(String language) {
        onView(withId(R.id.actionSettings)).perform(click());

        loadData(mActivityRule.getActivity(), language);

        try {
            onView(withText(currentHashMap.get(TestConstantKeys.LANGUAGE))).perform(click());
            onData(hasToString(startsWith(currentHashMap.get("otherLanguage")))).perform(click());
            onView(withId(R.id.actionSettings)).perform(click());
        } catch (Exception ignored) {
        }

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get(TestConstantKeys.LANGUAGE)))).perform(click());

        SystemClock.sleep(2000);

        mDevice.pressBack();

        SystemClock.sleep(2000);

        mActivityRule.launchActivity(new Intent());

        onView(withText(currentHashMap.get("sensors"))).perform(click());

        mDevice.waitForIdle();

        Espresso.pressBack();

        //mDevice.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();

        onView(withText(currentHashMap.get(TestConstantKeys.LANGUAGE))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get(TestConstantKeys.LANGUAGE)))).perform(click());

        onView(withText(currentHashMap.get(TestConstantKeys.LANGUAGE))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get(TestConstantKeys.LANGUAGE)))).perform(click());
    }
}
