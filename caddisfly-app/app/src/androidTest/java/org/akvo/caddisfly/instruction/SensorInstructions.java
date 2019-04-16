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

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SensorInstructions {

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

//        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);
//
//        clearPreferences(mActivityTestRule);

    }

    @Test
    @RequiresDevice
    public void testInstructionsAllSensors() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.SENSOR);

        for (int i = 0; i < TestConstants.SENSOR_TESTS_COUNT; i++) {

            TestInfo testInfo = testList.get(i);
            assertEquals(testInfo.getSubtype(), TestType.SENSOR);

            String id = testInfo.getUuid();
            id = id.substring(id.lastIndexOf("-") + 1);

            navigateToTest(i, id);

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

            onView(withText(testInfo.getName())).check(matches(isDisplayed()));

            mDevice.pressBack();

//            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }

//        Log.e("Caddisfly", jsArrayString.toString());

    }

    private void navigateToTest(int index, String id) {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Sensors");

        clickExternalSourceButton(index);

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

    }

    @Test
    @RequiresDevice
    public void testInstructionsAllManualColorSelect() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.MANUAL_COLOR_SELECT);

        for (int i = 0; i < TestConstants.MANUAL_SELECT_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.MANUAL_COLOR_SELECT);

            String uuid = testList.get(i).getUuid();
            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("ac33b44f9992").contains(id))
//
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
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

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(600);
                break;
            }
        }
        return pages + 1;
    }
}
