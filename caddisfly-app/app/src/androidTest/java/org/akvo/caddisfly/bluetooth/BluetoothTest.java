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

package org.akvo.caddisfly.bluetooth;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BluetoothTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION);

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

    @Test
    @RequiresDevice
    public void bluetoothTest() {

        activateTestMode();

        goToMainScreen();

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonBluetooth), withText("MD 610 Photometer"),
                        childAtPosition(
                                allOf(withId(R.id.mainLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_types),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(25, click()));


        onView(withText("Next")).perform(click());


        onView(withText("Test Selected")).perform(click());

//        if (TestUtil.isEmulator()) {
//            onView(withText("Bluetooth not supported."))
//                    .inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow()
//                            .getDecorView())))).check(matches(isDisplayed()));
//            return;
//        }
//
//        try {
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        onView(withText("Instructions")).perform(click());

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.image_pageRight),
                        childAtPosition(
                                allOf(withId(R.id.layout_footer),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatImageView.perform(click());

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).perform(click());

        onView(withText("Add exactly 2 ml SPADNS reagent solution to the water sample. (Caution: Vial is filled up to the top!)"))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageView2 = onView(
                allOf(withId(R.id.image_pageRight),
                        childAtPosition(
                                allOf(withId(R.id.layout_footer),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatImageView2.perform(click());

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageView3 = onView(
                allOf(withId(R.id.image_pageRight),
                        childAtPosition(
                                allOf(withId(R.id.layout_footer),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatImageView3.perform(click());

        onView(withText("Wipe the outside of the vial, hold vial at the lid and place vial in the chamber. Align arrow marks and press (*test*)"))
                .check(matches(isDisplayed()));

        TestUtil.swipeRight();

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()));

        TestUtil.goBack();

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        isDisplayed()));
        appCompatButton2.perform(click());

        if (TestUtil.isEmulator()) {
            onView(withText("Bluetooth not supported."))
                    .inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow()
                            .getDecorView())))).check(matches(isDisplayed()));
            return;
        }

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("Retry"),
                        childAtPosition(
                                allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton3.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.button_connect), withText("Connect"))).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Select Fluoride test on MD610"))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                allOf(withId(R.id.selectTestLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatButton5.perform(click());

        onView(withText("Press and hold (*shift*) + (*1*)(*7*)(*0*) to select Fluoride test and then press (*enter*)"))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton6.perform(click());

        onView(withText("Select Fluoride test on MD610"))
                .check(matches(isDisplayed()));

        onView(withText(R.string.test_selected)).perform(click());

        onView(withText("Awaiting result"))
                .check(matches(isDisplayed()));

        onView(withText("Test Instructions")).perform(click());

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).perform(click());

        onView(withText("Add exactly 2 ml SPADNS reagent solution to the water sample. (Caution: Vial is filled up to the top!)"))
                .check(matches(isDisplayed()));


        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText("Awaiting result"))
                .check(matches(isDisplayed()));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.textName1), withText("Fluoride"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                withId(R.id.layoutResult),
                                                0)),
                                0),
                        isDisplayed()));
        textView7.check(matches(withText("Fluoride")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.textResult1), withText(">2"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                withId(R.id.layoutResult),
                                                0)),
                                1),
                        isDisplayed()));
        textView8.check(matches(withText(">2")));

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.textResult1), withText(">2"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                withId(R.id.layoutResult),
                                                0)),
                                1),
                        isDisplayed()));
        textView9.check(matches(withText(">2")));

        ViewInteraction textView10 = onView(
                allOf(withId(R.id.textUnit1), withText("mg/l"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                withId(R.id.layoutResult),
                                                0)),
                                2),
                        isDisplayed()));
        textView10.check(matches(withText("mg/l")));

        onView(withId(R.id.button_accept_result)).perform(click());

    }
}
