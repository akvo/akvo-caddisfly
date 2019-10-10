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
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.DrawableMatcher.Companion.hasDrawable
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.viewmodel.TestListViewModel
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresExternalApp
@LargeTest
@RunWith(AndroidJUnit4::class)
class ManualInstructions : BaseTest() {

    private val jsArrayString = StringBuilder()

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)


    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
    }

    @Test
    fun testInstructionsSwatchSelect() {

        goToMainScreen()

        val viewModel = ViewModelProviders.of(mActivityTestRule.activity).get(TestListViewModel::class.java)

        val testList = viewModel.getTests(TestType.MANUAL_COLOR_SELECT)

        for (i in 0 until TestConstants.MANUAL_SELECT_TESTS_COUNT) {
            val testInfo = testList[i]

            var id = testInfo.uuid
            id = id.substring(id.lastIndexOf("-") + 1)

            var pages = navigateToTest("Testers", i, id)

            sleep(500)

            val customShapeButton = onView(allOf<View>(withId(R.id.swatch_select), isDisplayed()))

            customShapeButton.perform(clickPercent(0.1f, 0.7f))

            customShapeButton.perform(clickPercent(0.9f, 0.3f))

            takeScreenshot(id, pages)

            nextPage()

            takeScreenshot(id, ++pages)

            TestHelper.clickSubmitButton()

            mDevice.pressBack()

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],")
        }

        mActivityTestRule.finishActivity()

        mDevice.pressBack()

        mDevice.pressBack()

        Log.d("CaddisflyTests", jsArrayString.toString())
    }

    @Suppress("SameParameterValue")
    private fun navigateToTest(tabName: String, index: Int, id: String): Int {

        gotoSurveyForm()

        nextSurveyPage(tabName)

        clickExternalSourceButton(index)

        mDevice.waitForIdle()

        sleep(200)

        takeScreenshot(id, -1)

        mDevice.waitForIdle()

        onView(withId(R.id.imageBrand)).check(matches(hasDrawable()))

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        var pages = 0
        for (i in 0..16) {

            try {

                nextPage()

                sleep(200)

                pressBack()

                sleep(200)

                takeScreenshot(id, i)

                sleep(100)

                nextPage()

                sleep(200)

                pages++

            } catch (e: Exception) {
                sleep(200)
                break
            }

        }
        return pages
    }

    @Test
    fun instructionTest() {

        goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Meter")

        clickExternalSourceButton(0)

        sleep(1000)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.sd_on)).check(matches(isDisplayed()))

        onView(withText(R.string.sd_50_dip_sample_1)).check(matches(isDisplayed()))

        mActivityTestRule.finishActivity()
    }
}
