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

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.assertBackgroundColor
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
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
class CbtSurveyTest : BaseTest() {

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

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA")

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mActivityTestRule)
//        stubCameraIntent()
    }

//    private fun stubCameraIntent() {
//        val result = createImageCaptureStub()
//        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)
//    }

//    private fun createImageCaptureStub(): Instrumentation.ActivityResult {
//        // Put the drawable in a bundle.
//        val bundle = Bundle()
//        bundle.putParcelable("data", BitmapFactory.decodeResource(
//                mIntentsRule.activity.resources, R.drawable.closer))
//
//        // Create the Intent that will include the bundle.
//        val resultData = Intent()
//        resultData.putExtras(bundle)
//
//        // Create the ActivityResult with the Intent.
//        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
//    }

    @Test
    fun cbt_Survey_Test() {

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

        mActivityTestRule.finishActivity()

        sleep(3000)

        assertNotNull(mDevice.findObject(By.text("$result1: Very High Risk / Unsafe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: >100 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 9435.1 ")))
    }

    @Test
    fun cbt_Survey_DilutionTest() {

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

        mActivityTestRule.finishActivity()

        sleep(2500)

        assertNotNull(mDevice.findObject(By.text("$result1: Very Unsafe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: >1000 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 94351.0 ")))
    }

    @Test
    fun cbt_Survey() {

        sleep(1000)

        if (!skipOpeningExternalApp(Build.VERSION.SDK_INT)) {
            onView(allOf(withId(R.id.button_next), withText(R.string.next))).perform(click())
            onView(allOf(withId(R.id.button_ok), withText(R.string.go_to_external_app))).perform(click())
            sleep(2000)
            mActivityTestRule.launchActivity(Intent())
        }

        gotoSurveyForm()

        nextSurveyPage("Coliforms")

        clickExternalSourceButton(0)

        sleep(2000)

        val materialButton = onView(
                allOf(withId(R.id.button_phase_2), withText(R.string.submitResult),
                        TestUtil.childAtPosition(
                                allOf(withId(R.id.layoutPrepareSubmit),
                                        TestUtil.childAtPosition(
                                                withClassName(Matchers.`is`("android.widget.LinearLayout")),
                                                2)),
                                1),
                        isDisplayed()))
        materialButton.perform(click())

        onView(allOf(withId(R.id.takePhoto), withText(R.string.takePhoto),
                TestUtil.childAtPosition(
                        TestUtil.childAtPosition(
                                withId(R.id.viewPager),
                                0),
                        3),
                isDisplayed()))

        nextPage(3)

        onView(withText(R.string.skip)).check(ViewAssertions.doesNotExist())

        onView(withId(R.id.textResult1)).check(matches(withText("0")))

        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        assertBackgroundColor(R.id.layoutRisk, R.color.safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.safe)

        val riskText = getString(R.string.low_risk_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lowRisk = riskText[0].trim { it <= ' ' }
        val safe = riskText[1].trim { it <= ' ' }

        onView(withId(R.id.textRisk)).check(matches(withText(lowRisk)))
        onView(withId(R.id.textSubRisk)).check(matches(withText(safe)))

        nextPage()

        onView(withText(R.string.skip)).check(ViewAssertions.doesNotExist())

        onView(withText(R.string.round_off_test)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.buttonSubmit), isDisplayed())).check(matches(isDisplayed()))

        TestUtil.prevPage(2)

        sleep(2000)

        val customShapeButton = onView(allOf(withId(R.id.compartments),
                isDisplayed()))

        customShapeButton.perform(clickPercent(0.1f, 0.5f))
        customShapeButton.perform(clickPercent(0.3f, 0.5f))
        customShapeButton.perform(clickPercent(0.5f, 0.5f))
        customShapeButton.perform(clickPercent(0.7f, 0.1f))
        customShapeButton.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        nextPage()

        onView(withText(R.string.skip)).check(ViewAssertions.doesNotExist())

        onView(withId(R.id.textResult1)).check(matches(withText(">100")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        val riskText2 = getString(R.string.very_high_risk_unsafe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val veryHighRisk = riskText2[0].trim { it <= ' ' }
        val unsafe = riskText2[1].trim { it <= ' ' }

        onView(withId(R.id.textRisk)).check(matches(withText(veryHighRisk)))
        onView(withId(R.id.textSubRisk)).check(matches(withText(unsafe)))

        assertBackgroundColor(R.id.layoutRisk, R.color.unsafe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.unsafe)

        TestUtil.prevPage()

        sleep(2000)

        val button = onView(allOf(withId(R.id.compartments),
                isDisplayed()))

        button.perform(clickPercent(0.5f, 0.5f))
        button.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        nextPage()

        onView(withText(R.string.skip)).check(ViewAssertions.doesNotExist())

        sleep(1000)

        onView(withId(R.id.textResult1)).check(matches(withText("4")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        val riskText3 = getString(R.string.intermediate_possibly_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val intermediate = riskText3[0].trim { it <= ' ' }
        val possiblySafe = riskText3[1].trim { it <= ' ' }

        onView(withId(R.id.textRisk)).check(matches(withText(intermediate)))
        onView(withId(R.id.textSubRisk)).check(matches(withText(possiblySafe)))

        assertBackgroundColor(R.id.layoutRisk, R.color.possibly_safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.possibly_safe)

        nextPage()

        onView(withText(R.string.skip)).check(ViewAssertions.doesNotExist())

        sleep(5000)

        val result1 = getString(R.string.health_risk_category)
        val interval = getString(R.string.confidenceInterval)

        TestHelper.clickSubmitButton()

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("$result1: Intermediate Risk / Possibly Safe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: 4 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 10.94 ")))
    }
}
