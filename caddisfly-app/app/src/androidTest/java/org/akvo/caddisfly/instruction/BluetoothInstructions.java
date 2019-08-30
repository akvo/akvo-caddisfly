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

import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
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
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;

@SuppressWarnings("PMD.NcssMethodCount")
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BluetoothInstructions {
    private final StringBuilder jsArrayString = new StringBuilder();
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

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

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityRule);
    }

    @Test
    @RequiresDevice
    public void testInstructionsAll() {

        activateTestMode();

        mDevice.waitForWindowUpdate("", 2000);

        gotoSurveyForm();

        TestUtil.nextSurveyPage("MD610");

        clickExternalSourceButton(2);

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        List<TestInfo> testList = testConfigRepository.getTests(TestType.BLUETOOTH);
        for (int i = 0; i < TestConstants.MD610_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.BLUETOOTH);

            String id = testList.get(i).getUuid();

            id = id.substring(id.lastIndexOf("-") + 1);

            if (id.equalsIgnoreCase("e14626afa5b0")) {
                int pages = navigateToTest(3, id);

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
            }

        }

        mActivityRule.finishActivity();

        Log.d("Caddisfly", jsArrayString.toString());

    }

    private int navigateToTest(int index, String id) {

//        ViewInteraction recyclerView = onView(
//                allOf(withId(R.id.list_types),
//                        childAtPosition(
//                                withClassName(is("android.widget.LinearLayout")),
//                                0)));
//        recyclerView.perform(actionOnItemAtPosition(index, click()));

//        if (TestUtil.isEmulator()) {
//            onView(withText("Bluetooth not supported."))
//                    .inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow()
//                            .getDecorView())))).check(matches(isDisplayed()));
//            return;
//        }

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

//        TestUtil.sleep(5000);

//        onView(allOf(withId(R.id.button_connect), withText("Connect"))).perform(click());

//        TestUtil.sleep(2000);

        onView(withText(R.string.test_selected)).perform(click());

        //onView(withText(R.string.instructions)).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                TestUtil.sleep(6000);

                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());


            } catch (Exception e) {
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                Espresso.pressBack();
                TestUtil.sleep(300);
                break;
            }
        }
        return pages;
    }
}
