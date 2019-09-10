package org.akvo.caddisfly.test

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.isPatchAvailable
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class StriptestTest {

    companion object {

        private const val surveyTab = "Striptest"

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
    fun setUp() {
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
    }

    @Test
    @RequiresDevice
    fun startStriptest() {

        activateTestMode()

        sleep(10000)

        test5in1()
        testSoilNitrogen()
        testMerckPH()
        testNitrate100()
        testMercury()
    }

    private fun test5in1() {

        gotoSurveyForm()

        nextSurveyPage(surveyTab)

        clickExternalSourceButton(3)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(6000)

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.skip)).perform(click())

        sleep(1000)

        onView(withText(R.string.skip)).check(doesNotExist())

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        onView(withText(R.string.skip)).check(doesNotExist())

        val appCompatButton4 = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        appCompatButton4.perform(click())

        sleep(36000)

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        val buttonStart = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        buttonStart.perform(click())

        sleep(35000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        if (isPatchAvailable) {
            onView(withText("Total Chlorine")).check(matches(isDisplayed()))
            onView(withText("0 mg/l")).check(matches(isDisplayed()))
            onView(withText("Free Chlorine")).check(matches(isDisplayed()))
            onView(withText("0.15 mg/l")).check(matches(isDisplayed()))
            onView(withText("Total Hardness")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText(R.string.no_result)).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText("Total Alkalinity")).check(matches(isDisplayed()))
            onView(withText("32 mg/l")).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText("pH")).check(matches(isDisplayed()))
            onView(withText("6.2")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        onView(allOf<View>(withId(R.id.buttonSubmit), withText("Submit Result"),
                childAtPosition(
                        allOf<View>(withId(R.id.layoutFooter),
                                childAtPosition(
                                        withClassName(`is`("android.widget.RelativeLayout")),
                                        1)),
                        0),
                isDisplayed())).perform(click())

        mActivityRule.finishActivity()

        sleep(2000)

        if (isPatchAvailable) {
            assertNotNull(mDevice.findObject(By.text("Total Chlorine: 0.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Free Chlorine: 0.15 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Total Hardness:  mg/l")))
            assertNotNull(mDevice.findObject(By.text("Total Alkalinity: 32.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("pH: 6.2 ")))
        }
    }

    private fun testSoilNitrogen() {

        gotoSurveyForm()

        nextSurveyPage("Soil Striptest")

        clickExternalSourceButton(0)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        val buttonStart = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        buttonStart.perform(click())

        sleep(65000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        if (isPatchAvailable) {
            onView(withText("Nitrogen")).check(matches(isDisplayed()))
            onView(withText("205.15 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrate Nitrogen")).check(matches(isDisplayed()))
            onView(withText("41 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrite Nitrogen")).check(matches(isDisplayed()))
            onView(withText("0.03 mg/l")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        onView(allOf<View>(withId(R.id.buttonSubmit), withText("Submit Result"),
                childAtPosition(
                        allOf<View>(withId(R.id.layoutFooter),
                                childAtPosition(
                                        withClassName(`is`("android.widget.RelativeLayout")),
                                        1)),
                        0),
                isDisplayed())).perform(click())

        mActivityRule.finishActivity()

        sleep(2000)

        if (isPatchAvailable) {
            assertNotNull(mDevice.findObject(By.text("Nitrogen: 205.15 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Nitrate Nitrogen: 41.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Nitrite Nitrogen: 0.03 mg/l")))
        }
    }

    private fun testMerckPH() {

        gotoSurveyForm()

        nextSurveyPage(surveyTab)

        clickExternalSourceButton(2)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        val buttonStart = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        buttonStart.perform(click())

        sleep(5000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        if (isPatchAvailable) {
            onView(withText("pH")).check(matches(isDisplayed()))
            onView(withText("4.8")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_result)).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.buttonSubmit), withText("Submit Result"),
                childAtPosition(
                        allOf<View>(withId(R.id.layoutFooter),
                                childAtPosition(
                                        withClassName(`is`("android.widget.RelativeLayout")),
                                        1)),
                        0),
                isDisplayed())).perform(click())

        if (isPatchAvailable) {
            assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")))
        }
    }

    private fun testNitrate100() {

        gotoSurveyForm()

        nextSurveyPage(surveyTab)

        clickExternalSourceButton(5)

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }
        }

        val buttonStart = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        buttonStart.perform(click())

        sleep(60000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        if (isPatchAvailable) {
            onView(withText("Nitrate")).check(matches(isDisplayed()))
            onView(withText("14.5 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrite")).check(matches(isDisplayed()))
            onView(withText("1.85 mg/l")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        onView(allOf<View>(withId(R.id.buttonSubmit), withText("Submit Result"),
                childAtPosition(
                        allOf<View>(withId(R.id.layoutFooter),
                                childAtPosition(
                                        withClassName(`is`("android.widget.RelativeLayout")),
                                        1)),
                        0),
                isDisplayed())).perform(click())
    }

    private fun testMercury() {

        gotoSurveyForm()

        nextSurveyPage(surveyTab)

        val listView = UiScrollable(UiSelector())
        try {
            listView.scrollToEnd(1)
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }

        clickExternalSourceButton(1)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        val buttonStart = onView(
                allOf<View>(withId(R.id.buttonStart), withText("Start"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                4),
                        isDisplayed()))
        buttonStart.perform(click())

        sleep(35000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))
        onView(withText("Mercury")).check(matches(isDisplayed()))
        onView(withText("5 ug/l")).check(matches(isDisplayed()))

        onView(withId(R.id.image_result)).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.buttonSubmit), withText("Submit Result"),
                childAtPosition(
                        allOf<View>(withId(R.id.layoutFooter),
                                childAtPosition(
                                        withClassName(`is`("android.widget.RelativeLayout")),
                                        1)),
                        0),
                isDisplayed())).perform(click())

        mActivityRule.finishActivity()

        sleep(2000)

        mDevice.swipe(200, 750, 200, 600, 4)

        assertNotNull(mDevice.findObject(By.text("Mercury: 5.0 ug/l")))
    }
}
