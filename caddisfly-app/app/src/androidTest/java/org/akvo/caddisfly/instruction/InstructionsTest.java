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

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class InstructionsTest {

    private final StringBuilder jsArrayString = new StringBuilder();
    private final StringBuilder listString = new StringBuilder();

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<TestActivity> mActivityRule =
            new ActivityTestRule<>(TestActivity.class, false, false);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    @Before
    public void setUp() {

    }

    @Test
    @RequiresDevice
    public void testInstructionsMd610() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        List<TestInfo> testList = testConfigRepository.getTests(TestType.BLUETOOTH);
        for (int i = 0; i < TestConstants.MD610_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.BLUETOOTH);

            String uuid = testList.get(i).getUuid();

            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("9991fb84dd90 606b771e0ffe 6060e4dbe59d").contains(id))
//
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
                intent.putExtras(data);

                mActivityRule.launchActivity(intent);

                int pages = navigateToBluetoothTest(id);

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");

                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");

                TestHelper.getCurrentActivity().finish();
                mActivityRule.finishActivity();
            }

        }

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());
    }

    private int navigateToBluetoothTest(String id) {

        TestUtil.sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, -1);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        TestUtil.sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, 0);

        onView(withText(R.string.test_selected)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(1000);

                takeScreenshot(id, i + 1);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestHelper.navigateUp();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                break;
            }
        }
        return pages + 1;
    }

    @Test
    @RequiresDevice
    public void testInstructionsManual() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        List<TestInfo> testList = testConfigRepository.getTests(TestType.MANUAL);
        for (int i = 0; i < TestConstants.MANUAL_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.MANUAL);

            String uuid = testList.get(i).getUuid();

            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("fcdae725a518 57a6ced96c17 3752f1af4519").contains(id))
//                    || testList.get(i).getBrand().contains("Tester")
//                            || testList.get(i).getBrand().contains("SD")
//                            || testList.get(i).getBrand().contains("Tube"))
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
                intent.putExtras(data);

                mActivityRule.launchActivity(intent);

                int pages = navigateToTest(id, testList.get(i).getHasEndInstruction());

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");

                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");

                TestHelper.getCurrentActivity().finish();
                mActivityRule.finishActivity();
            }
        }

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());

    }

    private int navigateToTest(String id, boolean hasEndPage) {

        mDevice.waitForIdle();

        TestUtil.sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, -1);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_next), withText("Next"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(1000);

                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
//                pages++;

//                onView(withId(R.id.editResult)).check(matches(isDisplayed()))
//                        .perform(replaceText("1"), closeSoftKeyboard());
//
//                SystemClock.sleep(500);
//
//                takeScreenshot(id, i + 1);
//
//                onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

                if (hasEndPage) {

                    pages++;

                    takeScreenshot(id, i + 1);

                }
                TestUtil.sleep(300);
                break;
            }
        }
        return pages + 1;
    }
}