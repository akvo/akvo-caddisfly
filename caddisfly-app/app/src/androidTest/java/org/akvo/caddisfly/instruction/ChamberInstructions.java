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

package org.akvo.caddisfly.instruction;


import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.TestConstantKeys;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.sleep;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ChamberInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
//        prefs.edit().clear().apply();

        resetLanguage();
    }

    @Test
    @RequiresDevice
    public void testInstructionsBackcase() {

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(0);

        sleep(1000);

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        String id = Constants.FLUORIDE_ID.substring(
                Constants.FLUORIDE_ID.lastIndexOf("-") + 1, Constants.FLUORIDE_ID.length());

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

//        onView(withText(getString(mActivityTestRule.getActivity(), R.string.instructions))).perform(click());
//
//        for (int i = 0; i < 17; i++) {
//
//            try {
//                takeScreenshot(id, i);
//
//                onView(withId(R.id.image_pageRight)).perform(click());
//
//            } catch (Exception e) {
//                TestUtil.sleep(600);
//                Espresso.pressBack();
//                break;
//            }
//        }
    }

    @Test
    @RequiresDevice
    public void testInstructionsBackcase2() {

        goToMainScreen();

        gotoSurveyForm();

        clickExternalSourceButton(TestConstantKeys.NEXT);

        clickExternalSourceButton(0);

        sleep(1000);

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        String id = Constants.FREE_CHLORINE_ID.substring(
                Constants.FREE_CHLORINE_ID.lastIndexOf("-") + 1, Constants.FREE_CHLORINE_ID.length());

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

//        onView(withText(getString(mActivityTestRule.getActivity(), R.string.instructions))).perform(click());

//        for (int i = 0; i < 17; i++) {
//
//            try {
//                takeScreenshot(id, i);
//
//                onView(withId(R.id.image_pageRight)).perform(click());
//
//            } catch (Exception e) {
//                TestUtil.sleep(600);
//                Espresso.pressBack();
//                break;
//            }
//        }
    }

}
