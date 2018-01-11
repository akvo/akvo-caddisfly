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


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestHelper;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CbtInstructions {

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

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
        prefs.edit().clear().apply();

        resetLanguage();
    }

    private static void CheckTextInTable(@StringRes int resourceId) {
        ViewInteraction textView3 = onView(
                allOf(withText(resourceId),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText(resourceId)));
    }

    private static void CheckTextInTable(String text) {
        ViewInteraction textView3 = onView(
                allOf(withText(text),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText(text)));
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
    @FlakyTest
    public void cbtInstructions() {

        goToMainScreen();

        onView(withText("E.coli - Aquagenx CBT")).perform(click());

        ViewInteraction textView = onView(
                allOf(withText("www.aquagenx.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("www.aquagenx.com")));

        ViewInteraction button = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textToolbarTitle),
                childAtPosition(
                        allOf(withId(R.id.toolbar),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        0)),
                        1),
                isDisplayed()));
//        textView2.check(matches(withText("E.coli – Aquagenx CBT")));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_instructions),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                1),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.button_instructions),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                1),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        CheckTextInTable(R.string.put_on_gloves);

        CheckTextInTable(R.string.open_growth_medium_sachet);

        onView(withContentDescription("1")).check(matches(hasDrawable()));

        ViewInteraction imageView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                IsInstanceOf.instanceOf(android.widget.ScrollView.class),
                                0),
                        3),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.pager_indicator),
                childAtPosition(
                        allOf(withId(R.id.layout_footer),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.RelativeLayout.class),
                                        1)),
                        0),
                isDisplayed()));

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

        CheckTextInTable(R.string.dissolve_medium_in_sample);

        onView(withContentDescription("2")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        CheckTextInTable(TestHelper.getString(mActivityTestRule.getActivity(), R.string.medium_dissolves)
                + " " + TestHelper.getString(mActivityTestRule.getActivity(), R.string.when_medium_dissolved));

        onView(withContentDescription("3")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        CheckTextInTable(R.string.label_compartment_bag);

        onView(withContentDescription("4")).check(matches(hasDrawable()));

        TestUtil.nextPage(3);

        CheckTextInTable(R.string.let_incubate);

        onView(withContentDescription("7")).check(matches(hasDrawable()));

        ViewInteraction appCompatTextView14 = onView(
                allOf(withId(R.id.button_instructions), withText("Read Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        7),
                                2)));
        appCompatTextView14.perform(scrollTo(), click());

        onView(withText("Below 25 °C")).check(matches(isDisplayed()));

        onView(withText("Incubate in a portable incubator, preferably at 35 – 37 °C for 48 hours or put in or near another heat source.")).check(matches(isDisplayed()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        TestUtil.nextPage();

        CheckTextInTable(R.string.take_photo_of_incubated);

        onView(withContentDescription("8")).check(matches(hasDrawable()));

        mDevice.pressBack();

        onView(withId(R.id.button_instructions)).perform(click());

        CheckTextInTable(R.string.put_on_gloves);

        CheckTextInTable(R.string.open_growth_medium_sachet);

        onView(withContentDescription("1")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        CheckTextInTable(R.string.dissolve_medium_in_sample);

        onView(withContentDescription("2")).check(matches(hasDrawable()));

        TestUtil.nextPage();

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction button1 = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button1.check(matches(isDisplayed()));
    }


    @Test
    @FlakyTest
    public void cbtInstructionsAll() {

        goToMainScreen();

        onView(withText("E.coli - Aquagenx CBT")).perform(click());

        takeScreenshot("ed4db0fd3386", -1);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        for (int i = 0; i < 17; i++) {

            try {
                takeScreenshot("ed4db0fd3386", i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(600);
                Espresso.pressBack();
                TestUtil.sleep(600);
                Espresso.pressBack();
                break;
            }
        }

    }
}
