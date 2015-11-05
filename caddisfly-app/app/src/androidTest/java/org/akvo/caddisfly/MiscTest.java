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

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.DatePicker;

import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.ui.TypeListActivity;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.text.DecimalFormatSymbols;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.TestHelper.changeLanguage;
import static org.akvo.caddisfly.TestHelper.currentHashMap;
import static org.akvo.caddisfly.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.TestHelper.getActivityInstance;
import static org.akvo.caddisfly.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.TestHelper.leaveDiagnosticMode;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class MiscTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    //private UiDevice mDevice;

    @Before
    public void stubCameraIntent() {
        // Initialize UiDevice instance
        //mDevice = UiDevice.getInstance(getInstrumentation());

        changeLanguage("en");
    }

    @Test
    public void testSoftwareNotices() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.legalInformation)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.webNotices)).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.language)).perform(click());

        onView(withText(R.string.cancel)).perform(click());

    }

    @Test
    public void testSwatches() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.actionSwatches)).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionSwatches)).check(matches(isDisplayed()));

        Espresso.pressBack();
    }

    @Test
    public void testZErrors() {

//        getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                try {
//                    Method method = MainActivity.class.getDeclaredMethod("alertCameraFlashNotAvailable");
//                    method.setAccessible(true);
//                    method.invoke(getActivity());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    assertEquals(e.getMessage(), 0, 1);
//                }
//            }
//        });
//
//        //No flash
//        takeScreenshot();
//
//        onView(withId(android.R.id.button1)).perform(click());

        goToMainScreen();

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        final Activity typeListActivity = getActivityInstance();
        typeListActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Method method = TypeListActivity.class.getDeclaredMethod("alertFeatureNotSupported");
                    method.setAccessible(true);
                    method.invoke(typeListActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                    assertEquals(e.getMessage(), 0, 1);
                }
            }
        });

        onView(withId(android.R.id.button2)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withId(R.id.editExpiryDate)).perform(click());

        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(2025, 8, 25));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        final Activity activity = getActivityInstance();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Method method = ColorimetryLiquidActivity.class.getDeclaredMethod("alertCouldNotLoadConfig");
                    method.setAccessible(true);
                    method.invoke(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                    assertEquals(e.getMessage(), 0, 1);
                }
            }
        });

        onView(withId(android.R.id.button1)).perform(click());

        goToMainScreen();

    }
}
