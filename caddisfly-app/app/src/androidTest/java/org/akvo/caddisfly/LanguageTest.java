/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly;

import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.MainActivity;
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
import static org.akvo.caddisfly.TestHelper.currentHashMap;
import static org.akvo.caddisfly.TestHelper.loadData;
import static org.akvo.caddisfly.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.TestHelper.mDevice;
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

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion();

        onView(withText(version)).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
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
            onView(withText(currentHashMap.get("language"))).perform(click());
            onData(hasToString(startsWith(currentHashMap.get("otherLanguage")))).perform(click());
            onView(withId(R.id.actionSettings)).perform(click());
        } catch (Exception ignored) {
        }

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());

        onView(withText(currentHashMap.get("survey"))).perform(click());

        mDevice.pressBack();

        onView(withText(currentHashMap.get("sensors"))).perform(click());

        Espresso.pressBack();

        //mDevice.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();

        onView(withText(currentHashMap.get("language"))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());

        onView(withText(currentHashMap.get("language"))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }
}
