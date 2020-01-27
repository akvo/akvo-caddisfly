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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.DrawableMatcher.Companion.hasDrawable
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.TestUtil.prevPage
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class TesterSwatchSurveyTest : BaseTest() {

    private var scale: Float = 0.toFloat()

    companion object {
        @BeforeClass
        @JvmStatic
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mIntentsRule = ActivityTestRule(MainActivity::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mIntentsRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mIntentsRule)
        scale = mIntentsRule.activity.resources.displayMetrics.density
    }


    @Test
    fun tester_Survey_Chlorine_pH_LowRange_Cancel() {

        gotoSurveyForm()

        nextSurveyPage("Testers")

        clickExternalSourceButton(1)

        sleep(200)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        mDevice.waitForIdle()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()))

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click())

        sleep(500)

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()))

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

    }

    @Test
    fun tester_Survey_Chlorine_pH_LowRange() {

        gotoSurveyForm()

        nextSurveyPage("Testers")

        clickExternalSourceButton(0)

        sleep(200)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        mDevice.waitForIdle()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()))

        pressBack()

        sleep(200)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        mDevice.waitForIdle()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()))

        onView(withText(R.string.pt_fill_compartments_2))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()))

        pressBack()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        nextPage()

        prevPage()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        nextPage()

        nextPage()

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_3_reagent_1")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_4_reagent_2")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_5_shake")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_observe")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_6_interface_lr")).check(matches(hasDrawable()))

        sleep(200)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        sleep(500)

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.pt_open_lid)).check(matches(isDisplayed()))

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        sleep(200)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        mDevice.waitForIdle()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()))

        nextPage()

        onView(withText(R.string.pt_fill_compartments_1)).check(matches(isDisplayed()))

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()))

        sleep(500)

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click())

        sleep(500)

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        val customShapeButton = onView(allOf<View>(withId(R.id.swatch_select), isDisplayed()))

        customShapeButton.perform(clickPercent(0.1f, 0.55f))
        customShapeButton.perform(clickPercent(0.9f, 0.55f))

        sleep(200)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        onView(withText("pH")).check(matches(isDisplayed()))

        onView(withText("7.4")).check(matches(isDisplayed()))

        onView(withText("Chlorine")).check(matches(isDisplayed()))

        onView(withText("1 mg/l")).check(matches(isDisplayed()))

        pressBack()

        customShapeButton.perform(clickPercent(0.1f, 0.7f))

        customShapeButton.perform(clickPercent(0.9f, 0.3f))

        sleep(200)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        onView(withText("pH")).check(matches(isDisplayed()))

        onView(withText("7.2")).check(matches(isDisplayed()))

        onView(withText("Chlorine")).check(matches(isDisplayed()))

        onView(withText("2 mg/l")).check(matches(isDisplayed()))

        pressBack()

        sleep(200)

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        sleep(500)

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click())

        sleep(500)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        sleep(200)

        onView(withText(R.string.submitResult)).perform(click())

        mIntentsRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")))

        assertNotNull(mDevice.findObject(By.text("Chlorine: 2.0 mg/l")))

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun tester_Survey_HighRange() {

        gotoSurveyForm()

        nextSurveyPage("Testers")

        clickExternalSourceButton(1)

        sleep(200)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        mDevice.waitForIdle()

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        nextPage()

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_3_reagent_1")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_4_reagent_2")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_5_shake")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_observe")).check(matches(hasDrawable()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()))

        onView(withContentDescription("pt_6_interface_lr")).check(matches(hasDrawable()))

        sleep(200)

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        val customShapeButton = onView(allOf<View>(withId(R.id.swatch_select), isDisplayed()))

        customShapeButton.perform(clickPercent(0.1f, 0.55f))

        customShapeButton.perform(clickPercent(0.9f, 0.55f))

        sleep(200)

        nextPage()

        onView(withText("pH")).check(matches(isDisplayed()))

        onView(withText("7.4")).check(matches(isDisplayed()))

        onView(withText("Chlorine")).check(matches(isDisplayed()))

        onView(withText("2 mg/l")).check(matches(isDisplayed()))

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        pressBack()

        customShapeButton.perform(clickPercent(0.1f, 0.7f))

        customShapeButton.perform(clickPercent(0.9f, 0.3f))

        sleep(200)

        nextPage()

        onView(withText("pH")).check(matches(isDisplayed()))

        onView(withText("7.2")).check(matches(isDisplayed()))

        onView(withText("Chlorine")).check(matches(isDisplayed()))

        onView(withText("5 mg/l")).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        pressBack()

        sleep(200)

        nextPage()

        sleep(200)

        onView(withText(R.string.submitResult)).perform(click())

        mIntentsRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")))

        assertNotNull(mDevice.findObject(By.text("Chlorine: 5.0 mg/l")))

        mDevice.pressBack()

        mDevice.pressBack()
    }
}
