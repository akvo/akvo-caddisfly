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


import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestHelper;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
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

    @Before
    public void setup() {
        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        TestHelper.clearPreferences(mActivityTestRule);
    }

    @After
    public void tearDown() {
        TestHelper.clearPreferences(mActivityTestRule);
    }

    @Test
    @RequiresDevice
    public void testResultData() {

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
                onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

                TestUtil.sleep(1000);

                onView(withText(R.string.test_selected)).perform(click());

                onView(withText(R.string.skip)).perform(click());

                TestUtil.sleep(10000);

                onView(withText(R.string.result)).check(matches(isDisplayed()));

                onView(withText(">500")).check(matches(isDisplayed()));

                onView(withText("mg/l")).check(matches(isDisplayed()));

                onView(withId(R.id.button_submit_result)).check(matches(isDisplayed()));

                onView(withId(R.id.button_submit_result)).perform(click());

                break;
            }
        }

        assertNotNull(mDevice.findObject(By.text("3. Calcium Hardness")));

        assertNotNull(mDevice.findObject(By.text("Calcium Hardness: >500 mg/l")));

    }

    @Test
    @RequiresDevice
    public void bluetoothTest() {

        activateTestMode();

        gotoSurveyForm();

        TestUtil.nextSurveyPage("MD610");

        clickExternalSourceButton(1);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.test_selected)).perform(click());

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero_a))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        String phrase = getString(mActivityTestRule.getActivity(), R.string.add_reagent_1_liquid_exact_ml);
        phrase = phrase.replace("%reagent1", "2 ml SPADNS (467481)");
        onView(withText(phrase)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.wipe_place_align_vial_wait_2_a))
                .check(matches(isDisplayed()));

        TestUtil.swipeRight();

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()));

        TestUtil.goBack(4);

        TestHelper.navigateUp();

        TestHelper.clearPreferences(mActivityTestRule);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_prepare), withText(R.string.next),
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
                allOf(withId(android.R.id.button1), withText(R.string.retry),
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

        onView(withText("Select Test"))
                .check(matches(isDisplayed()));

        onView(withText("Press and hold (*shift*) + (*1*)(*7*)(*0*) to select Fluoride test and then press (*enter*)"))
                .check(matches(isDisplayed()));

        onView(withText(R.string.test_selected)).perform(click());

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero_a))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).perform(click());

        onView(withText(R.string.skip)).perform(click());

//        onView(withText("Awaiting result")).check(matches(isDisplayed()));

        try {
            Thread.sleep(6000);
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

        onView(withText(">2")).check(matches(isDisplayed()));

        onView(withText("mg/l")).check(matches(isDisplayed()));

        onView(withId(R.id.button_submit_result)).perform(click());
    }
}
