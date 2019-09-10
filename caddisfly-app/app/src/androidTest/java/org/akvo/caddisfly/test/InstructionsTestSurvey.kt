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

package org.akvo.caddisfly.test


import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class InstructionsTestSurvey {

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
        activateTestMode()
    }

    @Test
    fun instructionsTest() {

        gotoSurveyForm()

        nextSurveyPage("Soil Striptest")

        clickExternalSourceButton(1)

        mDevice.waitForIdle()

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        onView(withText(R.string.collect_5ml_mehlich_sample))
                .check(matches(isDisplayed()))

        onView(withText("Soil - Phosphorous"))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.add_5_drops_po4_1)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.swirl_and_mix)).check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_smaller_container)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.add_6_drops_po4_2)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.dip_strip_15_seconds_and_remove)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.dip_strip_15_seconds_in_reagent_and_remove)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_strip_clr)).check(matches(isDisplayed()))

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.prepare_test)).perform(click())

        pressBack()

        pressBack()

        sleep(1000)

        onView(withText(R.string.prepare_test)).check(matches(isDisplayed()))
    }

    @Test
    fun ironStripTestInstructions() {

        goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Striptest")

        clickExternalSourceButton(0)

        sleep(1000)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText("Water - Iron"))
                .check(matches(isDisplayed()))

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))

        onView(withText("Water - Iron"))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.open_one_foil_and_add_powder))
                .check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withId(R.id.viewPager)).perform(swipeLeft())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.prepare_test)).perform(click())

        pressBack()

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))

        pressBack()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))

    }
}
