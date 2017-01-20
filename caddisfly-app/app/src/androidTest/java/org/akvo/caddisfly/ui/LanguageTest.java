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

import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.util.TestConstant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null, TestType.COLORIMETRIC_LIQUID,
                new String[]{}, new String[]{}, new String[]{}, null, null));
    }

    @Test
    public void testLanguage() {

        onView(ViewMatchers.withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion();

        onView(withText(version)).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get(TestConstant.LANGUAGE)))).perform(click());
    }

    @Test
    public void testLanguageFrench() {
        languageTest("fr");
    }

    @Test
    public void testLanguageEnglish() {
        languageTest("en");
    }

    private void languageTest(String language) {
        onView(withId(R.id.actionSettings)).perform(click());

        loadData(mActivityRule.getActivity(), language);

        try {
            onView(withText(currentHashMap.get(TestConstant.LANGUAGE))).perform(click());
            onData(hasToString(startsWith(currentHashMap.get("otherLanguage")))).perform(click());
            onView(withId(R.id.actionSettings)).perform(click());
        } catch (Exception ignored) {
        }

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get(TestConstant.LANGUAGE)))).perform(click());

        SystemClock.sleep(2000);

        mDevice.pressBack();

        SystemClock.sleep(2000);

        mActivityRule.launchActivity(new Intent());

        onView(withText(currentHashMap.get("survey"))).perform(click());

        mDevice.pressBack();

        onView(withText(currentHashMap.get("sensors"))).perform(click());

        Espresso.pressBack();

        //mDevice.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();

        onView(withText(currentHashMap.get(TestConstant.LANGUAGE))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get(TestConstant.LANGUAGE)))).perform(click());

        onView(withText(currentHashMap.get(TestConstant.LANGUAGE))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get(TestConstant.LANGUAGE)))).perform(click());
    }
}
