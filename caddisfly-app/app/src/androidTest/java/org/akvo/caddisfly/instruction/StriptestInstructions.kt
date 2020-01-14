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

package org.akvo.caddisfly.instruction


import android.content.Intent
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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RequiresExternalApp
@LargeTest
@RunWith(AndroidJUnit4::class)
class StriptestInstructions : BaseTest() {

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
    var mActivityTestRule = ActivityTestRule(TestActivity::class.java, false, false)

    @Rule
    @JvmField
    var mMainActivityTestRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Before
    override fun setUp() {
        super.setUp()
        mMainActivityTestRule.launchActivity(Intent())
        loadData(mMainActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mMainActivityTestRule)

        activateTestMode()

        mMainActivityTestRule.finishActivity()
    }

    @Test
    fun instructionsTest() {

        gotoSurveyForm()

        nextSurveyPage("Striptest")

        sleep(500)

        clickExternalSourceButton(2)

        mDevice.waitForIdle()

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(7000)

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.collect_water_sample)).check(matches(isDisplayed()))

        onView(withText("Water - pH")).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.dip_strip_in_water_2_seconds)).check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withId(R.id.viewPager)).perform(swipeLeft())

        mDevice.waitForIdle()

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_strip_clr)).check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withText(R.string.collect_water_sample))
                .check(matches(isDisplayed()))

        onView(withText(R.string.skip)).perform(click())

        sleep(1500)

        TestHelper.clickStartButton()

        sleep(7000)

        if (isPatchAvailable()) {
            onView(withText("4.8")).check(matches(isDisplayed()))
            pressBack()
            onView(withText("pH")).check(matches(isDisplayed()))
        } else {
            onView(withText(R.string.no_strip_found)).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()

        sleep(2500)

        if (isPatchAvailable()) {
            assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")))
        } else {
            assertNotNull(mDevice.findObject(By.text("pH: null ")))
        }
    }
}
