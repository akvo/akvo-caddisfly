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


import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
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

import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.getString;
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
public class SensorInstructions {

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
    public void testInstructionsAll() {

        goToMainScreen();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.sensors))).perform(click());

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.SENSOR);

        for (int i = 0; i < TestConstants.SENSOR_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.SENSOR);

            String id = testList.get(i).getUuid();
            id = id.substring(id.lastIndexOf("-") + 1, id.length());

            int pages = navigateToTest(i, id);

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }

//        Log.e("Caddisfly", jsArrayString.toString());

    }

    private int navigateToTest(int index, String id) {

        int pages = 0;

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_types),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(index, click()));

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

//        onView(withText(getString(mActivityTestRule.getActivity(), R.string.instructions))).perform(click());
//
//        for (int i = 0; i < 17; i++) {
//            pages++;
//
//            try {
//                takeScreenshot(id, i);
//
//                onView(withId(R.id.image_pageRight)).perform(click());
//
//            } catch (Exception e) {
//                TestUtil.sleep(600);
//                Espresso.pressBack();
//                Espresso.pressBack();
//                TestUtil.sleep(600);
//                break;
//            }
//        }

        Espresso.pressBack();

        return pages;
    }
}
