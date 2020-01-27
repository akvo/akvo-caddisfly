package org.akvo.caddisfly.test


import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.assertBackgroundColor
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.Matchers.*
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RequiresExternalApp
@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtTCSurveyTest : BaseTest() {

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
                    "android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE")

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mActivityTestRule)
    }

    @Test
    fun cbt_Survey_TotalTest() {

        sleep(2000)

        if (!skipOpeningExternalApp(Build.VERSION.SDK_INT)) {
            onView(allOf(withId(R.id.button_next), withText(R.string.next))).perform(click())
            onView(allOf(withId(R.id.button_ok), withText(R.string.go_to_external_app))).perform(click())
            sleep(2000)
        }

        gotoSurveyForm()

        nextSurveyPage("Coliforms2")

        clickExternalSourceButton(0)

        sleep(2000)

        val materialButton = onView(
                allOf(withId(R.id.button_phase_2), withText(R.string.submitResult),
                        childAtPosition(
                                allOf(withId(R.id.layoutPrepareSubmit),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                2)),
                                1),
                        isDisplayed()))
        materialButton.perform(click())

        onView(allOf(withId(R.id.takePhoto), withText(R.string.takePhoto),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.viewPager),
                                0),
                        3),
                isDisplayed()))

        nextPage(3)

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.take_photo_incubated_tc)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withText(R.string.remember_to_shine)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

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

        onView(withText(R.string.skip)).check(doesNotExist())

        onView(withId(R.id.textResult1)).check(matches(withText("0")))
        onView(withId(R.id.textResult2)).check(matches(withText(">100")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        onView(withId(R.id.textRisk)).check(matches(not(isDisplayed())))
        onView(withId(R.id.textSubRisk)).check(matches(not(isDisplayed())))

        val riskText = getString(R.string.low_risk_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lowRisk = riskText[0].trim { it <= ' ' }
        val safe = riskText[1].trim { it <= ' ' }

        onView(withId(R.id.textRisk1)).check(matches(withText(lowRisk)))
        onView(withId(R.id.textSubRisk2)).check(matches(withText(safe)))

        assertBackgroundColor(R.id.layoutRisk, R.color.safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.safe)

        TestUtil.prevPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        sleep(2000)

        val button = onView(allOf(withId(R.id.compartments),
                isDisplayed()))

        button.perform(clickPercent(0.5f, 0.5f))
        button.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        sleep(1000)

        onView(withId(R.id.textResult1)).check(matches(withText("0")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))
        onView(withId(R.id.textResult2)).check(matches(withText("4")))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        onView(withId(R.id.textRisk1)).check(matches(withText(lowRisk)))
        onView(withId(R.id.textSubRisk2)).check(matches(withText(safe)))

        assertBackgroundColor(R.id.layoutRisk, R.color.safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.safe)

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        sleep(5000)

        val result1 = getString(R.string.health_risk_category)
        val interval = getString(R.string.confidenceInterval)

        TestHelper.clickSubmitButton()

        sleep(1000)

        assertNotNull(mDevice.findObject(By.text("$result1: Low Risk / Safe ")))
        assertNotNull(mDevice.findObject(By.text("MPN: 0 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("$interval: 2.87 ")))
        assertNotNull(mDevice.findObject(By.text("TC MPN: 4 MPN/100ml")))
        assertNotNull(mDevice.findObject(By.text("TC $interval: 10.94 ")))
    }
}
