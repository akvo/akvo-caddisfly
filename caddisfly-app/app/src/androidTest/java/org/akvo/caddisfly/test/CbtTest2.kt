package org.akvo.caddisfly.test


import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper.*
import org.akvo.caddisfly.util.TestUtil.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtTest2 {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (mDevice == null) {
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
    fun setUp() {
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
    }

    @Test
    fun testCbtResults() {

        val appCompatButton = onView(
                allOf(withId(R.id.button_next), withText("Next"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatButton2 = onView(
                allOf(withId(R.id.button_ok), withText("Go to Akvo Flow"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                4),
                        isDisplayed()))
        appCompatButton2.perform(click())

        sleep(2000)

        gotoSurveyForm()

        nextSurveyPage("Coliforms")

        clickExternalSourceButton(0)

        sleep(2000)

        val materialButton = onView(
                allOf(withId(R.id.button_phase_2), withText("Submit Result"),
                        childAtPosition(
                                allOf(withId(R.id.layoutPrepareSubmit),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                2)),
                                1),
                        isDisplayed()))
        materialButton.perform(click())

        onView(allOf(withId(R.id.takePhoto), withText("Take Photo"),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.viewPager),
                                0),
                        3),
                isDisplayed()))

        nextPage(3)

        onView(withId(R.id.textResult1)).check(matches(withText("0")))

        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        assertBackgroundColor(R.id.layoutRisk, R.color.safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.safe)

        onView(withId(R.id.textRisk)).check(matches(withText("Low Risk")))
        onView(withId(R.id.textSubRisk)).check(matches(withText("Safe")))

        nextPage()

        onView(withText("Round off test")).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.buttonSubmit), isDisplayed())).check(matches(isDisplayed()))

        goBack(2)

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

        onView(withId(R.id.textResult1)).check(matches(withText(">100")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        onView(withId(R.id.textRisk)).check(matches(withText("Very High Risk")))
        onView(withId(R.id.textSubRisk)).check(matches(withText("Unsafe")))

        assertBackgroundColor(R.id.layoutRisk, R.color.unsafe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.unsafe)

        prevPage()

        sleep(2000)

        val button = onView(allOf(withId(R.id.compartments),
                isDisplayed()))

        button.perform(clickPercent(0.5f, 0.5f))
        button.perform(clickPercent(0.9f, 0.1f))

        sleep(1000)

        nextPage()

        sleep(1000)

        onView(withId(R.id.textResult1)).check(matches(withText("4")))
        onView(withId(R.id.textUnit1)).check(matches(withText("MPN/100ml")))

        onView(withId(R.id.textRisk)).check(matches(withText("Intermediate Risk")))
        onView(withId(R.id.textSubRisk)).check(matches(withText("Possibly Safe")))

        assertBackgroundColor(R.id.layoutRisk, R.color.possibly_safe)
        assertBackgroundColor(R.id.layoutRisk2, R.color.possibly_safe)

    }

    private fun assertBackgroundColor(view: Int, color: Int) {
        val bar = getCurrentActivity().findViewById<View>(view)
        val actualColor = (bar.background as ColorDrawable).color
        val expectedColor = ContextCompat.getColor(getCurrentActivity(), color)
        assertEquals(actualColor, expectedColor)
    }
}
