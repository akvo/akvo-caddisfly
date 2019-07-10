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
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
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
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StriptestInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();
    private final StringBuilder listString = new StringBuilder();

    @Rule
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

        TestUtil.nextSurveyPage("Strip Tests");

        clickExternalSourceButton(2);

        mDevice.waitForIdle();

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(6000);

        onView(withText(R.string.skip)).check(matches(isDisplayed()));

        onView(withText(R.string.collect_water_sample)).check(matches(isDisplayed()));

        onView(withText("Water - pH")).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.skip)).check(matches(isDisplayed()));

        onView(withText(R.string.dip_strip_in_water_2_seconds)).check(matches(isDisplayed()));

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()));

        onView(withId(R.id.viewPager)).perform(swipeLeft());

        mDevice.waitForIdle();

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()));

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.place_strip_clr)).check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText(R.string.collect_water_sample))
                .check(matches(isDisplayed()));

        onView(withText(R.string.skip)).perform(click());

        sleep(1500);

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonStart), withText(R.string.start),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        sleep(7000);

        onView(withText("4.8")).check(matches(isDisplayed()));

        pressBack();

        onView(withText("pH")).check(matches(isDisplayed()));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.buttonDone), withText(R.string.submitResult),
                        childAtPosition(
                                allOf(withId(R.id.layoutFooter),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatButton5.perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")));
    }

    @Test
    public void testInstructionsAllStripTests() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.STRIP_TEST);

        for (int i = 0; i < TestConstants.STRIP_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.STRIP_TEST);

            String uuid = testList.get(i).getUuid();
            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

            if (("aa4a4e3100c9").contains(id))
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

                onView(allOf(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                3),
                        isDisplayed())).perform(click());

                sleep(2000);

                takeScreenshot(id, ++pages);

                sleep(30000);

                takeScreenshot(id, ++pages);

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

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(5000);

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
