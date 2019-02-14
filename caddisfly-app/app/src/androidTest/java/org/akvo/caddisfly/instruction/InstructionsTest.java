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
import android.os.Environment;
import android.support.test.espresso.Espresso;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
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

import java.io.File;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;

@RunWith(AndroidJUnit4.class)
public class InstructionsTest {

    private final StringBuilder jsArrayString = new StringBuilder();

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<TestActivity> mActivityRule =
            new ActivityTestRule<>(TestActivity.class, false, false);

//
//    public ActivityTestRule<TestActivity> mActivityRule =
//            new ActivityTestRule<TestActivity>(TestActivity.class) {
//
//                @Override
//                protected Intent getActivityIntent() {
//
//                    TestConfigRepository testConfigRepository = new TestConfigRepository();
//                    TestInfo testInfo = testConfigRepository.getTestInfo("072cab32-3e4c-41f3-8378-e14626afa5b0");
//
//                    Intent intent = new Intent();
//                    Bundle data = new Bundle();
//                    data.putParcelable(ConstantKey.TEST_INFO, testInfo);
//                    intent.putExtras(data);
//
//                    return intent;
//                }
//            };

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
    public void testInstructionsAll() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        List<TestInfo> testList = testConfigRepository.getTests(TestType.BLUETOOTH);
        for (int i = 0; i < TestConstants.MD610_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.BLUETOOTH);

            String uuid = testList.get(i).getUuid();

            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (id.equalsIgnoreCase("5a3d490d9df3")) {

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
//            }

            mActivityRule.finishActivity();
        }

        Log.d("Caddisfly", jsArrayString.toString());

    }

    private int navigateToBluetoothTest(String id) {

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.test_selected)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(1000);

                takeScreenshot(id, i);

                onView(withId(R.id.actionSkip)).check(matches(isDisplayed()));

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestHelper.navigateUp();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                break;
            }
        }
        return pages;
    }

    @Test
    @RequiresDevice
    public void testInstructionsManual() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        List<TestInfo> testList = testConfigRepository.getTests(TestType.MANUAL);
        for (int i = 0; i < TestConstants.MANUAL_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.MANUAL);

            String uuid = testList.get(i).getUuid();

            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

            if (id.equalsIgnoreCase("883bf6e9ff63")) {

                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
                intent.putExtras(data);

                mActivityRule.launchActivity(intent);

                int pages = navigateToTest(id);

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
            }

            mActivityRule.finishActivity();
        }

        Log.d("Caddisfly", jsArrayString.toString());

    }

    private int navigateToTest(String id) {

        onView(withText(R.string.instructions)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(1000);

                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                break;
            }
        }
        return pages;
    }
}