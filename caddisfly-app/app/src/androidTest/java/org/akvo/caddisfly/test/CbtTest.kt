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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.clickPercent
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
class CbtTest : BaseTest() {

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
    var mIntentsRule = IntentsTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE")

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mIntentsRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mIntentsRule)
        stubCameraIntent()
    }

    private fun stubCameraIntent() {
        val result = createImageCaptureStub()
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)
    }

    private fun createImageCaptureStub(): Instrumentation.ActivityResult {
        // Put the drawable in a bundle.
        val bundle = Bundle()
        bundle.putParcelable("data", BitmapFactory.decodeResource(
                mIntentsRule.activity.resources, R.drawable.closer))

        // Create the Intent that will include the bundle.
        val resultData = Intent()
        resultData.putExtras(bundle)

        // Create the ActivityResult with the Intent.
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Test
    fun cbtTest() {

        gotoSurveyForm()

        nextSurveyPage("Coliforms")

        clickExternalSourceButton(0)

        mDevice.waitForIdle()

        sleep(1000)

        onView(allOf<View>(withId(R.id.button_phase_2), withText(R.string.submitResult), isDisplayed()))
                .perform(click())

        sleep(1000)

        nextPage(2)

        mDevice.waitForIdle()

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        val customShapeButton = onView(allOf<View>(withId(R.id.compartments),
                isDisplayed()))

        customShapeButton.perform(clickPercent(0.1f, 0.5f))
        customShapeButton.perform(clickPercent(0.3f, 0.5f))
        customShapeButton.perform(clickPercent(0.5f, 0.5f))
        customShapeButton.perform(clickPercent(0.7f, 0.1f))
        customShapeButton.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        nextPage()

        val riskText = getString(R.string.very_high_risk_unsafe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val risk1 = riskText[0].trim { it <= ' ' }
        val risk2 = riskText[1].trim { it <= ' ' }

        onView(withText(risk1)).check(matches(isDisplayed()))

        onView(withText(risk2)).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(withText(">100")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText(">100")))
        onView(withId(R.id.textResult2)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        nextPage()

        val result1 = getString(R.string.health_risk_category)
        val interval = getString(R.string.confidenceInterval)

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(3000)

        assertNotNull(mDevice.findObject(By.text("$result1: Very High Risk / Unsafe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: >100 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 9435.1 ")))
    }

    @Test
    fun cbtDilutionTest() {

        gotoSurveyForm()

        nextSurveyPage("Coliforms")

        clickExternalSourceButton(1)

        mDevice.waitForIdle()

        sleep(1000)

        val appCompatButton2 = onView(
                allOf<View>(withId(R.id.button_phase_2), withText(R.string.submitResult),
                        isDisplayed()))
        appCompatButton2.perform(click())

        sleep(1000)

        nextPage(2)

        mDevice.waitForIdle()

        val customShapeButton = onView(
                allOf<View>(withId(R.id.compartments),
                        isDisplayed()))
        customShapeButton.perform(clickPercent(0.1f, 0.5f))
        customShapeButton.perform(clickPercent(0.3f, 0.5f))
        customShapeButton.perform(clickPercent(0.5f, 0.5f))
        customShapeButton.perform(clickPercent(0.7f, 0.1f))
        customShapeButton.perform(clickPercent(0.9f, 0.1f))

        sleep(100)

        nextPage()

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult1)).check(matches(withText(">1000")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText(">1000")))
        onView(withId(R.id.textResult2)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        pressBack()

        customShapeButton.perform(clickPercent(0.9f, 0.1f))

        sleep(3000)

        mDevice.waitForIdle()

        nextPage()

        var riskText = getString(R.string.very_high_risk_unsafe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var risk1 = riskText[0].trim { it <= ' ' }
        var risk2 = riskText[1].trim { it <= ' ' }

        onView(withText(risk1)).check(matches(isDisplayed()))
        onView(withText(risk2)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult1)).check(matches(withText("483")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText("483")))
        onView(withId(R.id.textResult2)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        pressBack()

        customShapeButton.perform(clickPercent(0.3f, 0.5f))

        sleep(3000)

        mDevice.waitForIdle()

        nextPage()

        riskText = getString(R.string.low_risk_possibly_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        risk1 = riskText[0].trim { it <= ' ' }
        risk2 = riskText[1].trim { it <= ' ' }
        onView(withText(risk1)).check(matches(isDisplayed()))
        onView(withText(risk2)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult1)).check(matches(withText("58")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText("58")))
        onView(withId(R.id.textResult2)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        pressBack()

        customShapeButton.perform(clickPercent(0.3f, 0.5f))
        customShapeButton.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        mDevice.waitForIdle()

        nextPage()

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult1)).check(matches(withText(">1000")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText(">1000")))
        onView(withId(R.id.textResult2)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        nextPage()

        sleep(5000)

        val result1 = getString(R.string.recreational_health_risk_category)
        val interval = getString(R.string.confidenceInterval)

        TestHelper.clickSubmitButton()

        mIntentsRule.finishActivity()

        sleep(2500)

        assertNotNull(mDevice.findObject(By.text("$result1: Very Unsafe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: >1000 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 94351.0 ")))
    }
}
