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

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RequiresExternalApp
@LargeTest
class BluetoothInstructions : BaseTest() {
    private val jsArrayString = StringBuilder()

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
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
    }

    @Test
    @RequiresDevice
    fun testInstructionsAll() {

        activateTestMode()

        mDevice.waitForWindowUpdate("", 2000)

        gotoSurveyForm()

        TestUtil.nextSurveyPage("MD610")

        clickExternalSourceButton(2)

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.BLUETOOTH)
        for (i in 0 until TestConstants.MD610_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.BLUETOOTH)

            var id = testList[i].uuid

            id = id.substring(id.lastIndexOf("-") + 1)

            if (id.equals("e14626afa5b0", ignoreCase = true)) {
                val pages = navigateToTest(id)

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],")
            }

        }

        mActivityRule.finishActivity()

        Log.d("Caddisfly", jsArrayString.toString())

    }

    private fun navigateToTest(id: String): Int {

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.test_selected)).perform(click())

        var pages = 0
        for (i in 0..16) {
            pages++

            try {
                sleep(6000)

                takeScreenshot(id, i)

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {
                sleep(300)
                Espresso.pressBack()
                sleep(300)
                Espresso.pressBack()
                sleep(300)
                Espresso.pressBack()
                sleep(300)
                break
            }

        }
        return pages
    }
}
