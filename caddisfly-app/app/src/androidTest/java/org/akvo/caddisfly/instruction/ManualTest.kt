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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.CoreMatchers.not
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
class ManualTest : BaseTest() {

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
    var mIntentsRule = IntentsTestRule(MainActivity::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mIntentsRule.activity, mCurrentLanguage)
        clearPreferences(mIntentsRule)
    }

    @Before
    fun stubCameraIntent() {
        val result = createImageCaptureStub()

        // Stub the Intent.
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)
    }

    private fun createImageCaptureStub(): Instrumentation.ActivityResult {
        // Put the drawable in a bundle.
        val bundle = Bundle()
        //        bundle.putParcelable("data", BitmapFactory.decodeResource(
        //                mIntentsRule.getActivity().getResources(), R.drawable.closer));

        // Create the Intent that will include the bundle.
        val resultData = Intent()
        resultData.putExtras(bundle)

        // Create the ActivityResult with the Intent.
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Test
    fun runManualTurbidityTest() {

        goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Meter")

        clickExternalSourceButton(2)

        sleep(1000)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.fill_turbidity_tube)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.empty_tube_see_black_circle)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        nextPage()

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.clean_turbidity_tube)).check(matches(isDisplayed()))

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.fill_turbidity_tube))
                .check(matches(isDisplayed()))

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.empty_tube_see_black_circle))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.enter_measured_value)).check(matches(isDisplayed()))

        pressBack()

        sleep(500)

        pressBack()

        onView(withText(R.string.skip)).perform(click())

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        sleep(500)

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("Turbidity: 1234.0 NTU")))

        mDevice.pressBack()

        mDevice.pressBack()
    }

    @Test
    fun runManualPhTest() {

        goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Meter")

        clickExternalSourceButton(0)

        sleep(1000)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_50_dip_sample_1))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.sd_50_dip_sample_2))
                .check(matches(isDisplayed()))

        nextPage()

        sleep(500)

        onView(withText(R.string.takePhoto)).check(matches(isDisplayed())).perform(click())

        sleep(1500)

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_rinse))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.sd_50_dip_sample_2))
                .check(matches(isDisplayed()))

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        pressBack()

        sleep(500)

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        sleep(1500)

        onView(withText(R.string.takePhoto)).perform(click())

        sleep(1500)

        onView(withText(R.string.next)).perform(click())

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("pH: 12.0 ")))

        mDevice.pressBack()

        mDevice.pressBack()

    }

    @Test
    fun runManualEcTest() {

        goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Meter")

        clickExternalSourceButton(1)

        sleep(1000)

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_70_dip_sample_1))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_70_dip_sample_2))
                .check(matches(isDisplayed()))

        nextPage()

        sleep(500)

        onView(withText(R.string.takePhoto)).check(matches(isDisplayed())).perform(click())

        sleep(1500)

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard())

        sleep(1000)

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("21"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withText("ms/cm")).perform(click())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).perform(replaceText("20"))

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_70_rinse))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()))

        onView(withText(R.string.sd_70_dip_sample_1))
                .check(matches(isDisplayed()))

        nextPage()

        pressBack()

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        sleep(500)

        onView(withText(R.string.takePhoto)).perform(click())

        sleep(1500)

        onView(withText(R.string.next)).perform(click())

        sleep(500)

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(2500)

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")))

        clickExternalSourceButton(1)

        sleep(500)

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        sleep(500)

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        sleep(2500)

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")))
    }

    @Test
    fun runManualEcTest2() {

        gotoSurveyForm()

        nextSurveyPage("Meter")

        clickExternalSourceButton(1)

        sleep(500)

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        sleep(1000)

        onView(withText(R.string.takePhoto)).perform(click())

        sleep(1500)

        onView(withId(R.id.editResult)).perform(replaceText("200"))

        sleep(500)

        onView(withText(R.string.next)).perform(click())

        onView(withText("μS/cm")).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("20001"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).perform(replaceText("1999"))

        onView(withText("ms/cm")).perform(click())

        onView(withText(R.string.next)).perform(click())

        onView(withText("μS/cm")).perform(click())

        onView(withText(R.string.next)).perform(click())

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 1999.0 μS/cm")))

        mDevice.pressBack()

        mDevice.pressBack()
    }
}
