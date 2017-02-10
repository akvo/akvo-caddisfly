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

package org.akvo.caddisfly.survey;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestConstant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SurveyTest {

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

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null,
                TestType.COLORIMETRIC_LIQUID, new String[]{}, new String[]{}, new String[]{}, null, null));

        resetLanguage();
    }

    @Test
    public void surveyQuestions() {

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton(TestConstant.NEXT);

        clickExternalSourceButton(0);

        SystemClock.sleep(12000);

        onView(allOf(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()));

        onView(withId(R.id.buttonAcceptResult)).perform(click());


        clickExternalSourceButton(1);

        SystemClock.sleep(12000);

        onView(allOf(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textTitle), withText("Soil - Electrical Conductivity"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()));

        onView(withId(R.id.buttonAcceptResult)).perform(click());


        clickExternalSourceButton(2);

        SystemClock.sleep(12000);

        onView(allOf(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textTitle), withText("Soil - Moisture"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit), withText("% VWC"))).check(matches(isDisplayed()));

        onView(withId(R.id.textUnit2)).check(matches(not(isDisplayed())));

        onView(withId(R.id.buttonAcceptResult)).perform(click());

    }

}

