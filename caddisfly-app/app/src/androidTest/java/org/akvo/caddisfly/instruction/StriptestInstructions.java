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


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.TestActivity;
import org.akvo.caddisfly.util.TestHelper;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StriptestInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();
    private final StringBuilder listString = new StringBuilder();

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<TestActivity> mActivityTestRule =
            new ActivityTestRule<>(TestActivity.class, false, false);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    @Before
    public void setUp() {
        mActivityTestRule.launchActivity(new Intent());
        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);
        clearPreferences(mActivityTestRule);
        mActivityTestRule.finishActivity();
    }

    @Test
    public void instructionsTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Soil Striptest");

        clickExternalSourceButton(1);

        mDevice.waitForIdle();

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText(R.string.instructions),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText(R.string.collect_5ml_mehlich_sample))
                .check(matches(isDisplayed()));

        onView(withText("Soil - Phosphorous"))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.add_5_drops_reagent_1))
                .check(matches(isDisplayed()));

//        onView(withText(R.string.swirl_and_mix))
//                .check(matches(isDisplayed()));

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()));

        onView(withId(R.id.viewPager))
                .perform(swipeLeft());

        onView(withText(R.string.put_6_drops_of_reagent_in_another_container))
                .check(matches(isDisplayed()));

        onView(withText(R.string.place_tube_in_provided_rack))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

//        onView(withText(R.string.dip_container_15s))
//                .check(matches(isDisplayed()));

//        onView(withText(R.string.shake_excess_water))
//                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        TestUtil.nextPage();

        onView(withText(R.string.place_strip_clr))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
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

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_instructions), withText(R.string.instructions),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton3.perform(click());

        pressBack();

//        ViewInteraction imageView3 = onView(
//                allOf(withId(R.id.imageBrand),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.coordinatorLayout),
//                                        0),
//                                1),
//                        isDisplayed()));
//        imageView3.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_prepare),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

    }

    @RequiresDevice
    public void testInstructionsAllStripTests() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.STRIP_TEST);

        for (int i = 0; i < TestConstants.STRIP_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.STRIP_TEST);

            String uuid = testList.get(i).getUuid();
            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("ac33b44f9992, 32d9b8f4aecf").contains(id))
//
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, mCurrentLanguage);
                intent.putExtras(data);

                mActivityTestRule.launchActivity(intent);

                int pages = navigateToTest(id);

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");

                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");

                TestHelper.getCurrentActivity().finish();
                mActivityTestRule.finishActivity();
            }
        }

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());

    }

    private int navigateToTest(String id) {

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withText(R.string.instructions)).check(matches(isDisplayed())).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            try {
                takeScreenshot(id, pages);

                pages++;

                if (("ac33b44f9992, 32d9b8f4aecf").contains(id)) {
                    try {

                        if (pages == 4) {
                            //noinspection ConstantConditions
                            if (mCurrentLanguage.equals("en")) {
                                mDevice.click(100, 600);
                            } else {
                                mDevice.click(450, 600);
                            }
                            sleep(600);
                            takeScreenshot(id, pages);
                            pages++;
                            sleep(600);
                            mDevice.pressBack();
                        }
                    } catch (Exception ignore) {
                    }
                }

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(600);
                break;
            }
        }
        return pages + 1;
    }
}
