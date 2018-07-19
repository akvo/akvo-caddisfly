package org.akvo.caddisfly.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.TestHelper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextSurveyPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IntroTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        } else {
            nextSurveyPage("Fluoride");
            mDevice.pressBack();
        }
    }

    @Test
    public void introTest() {

        onView(withId(R.id.button_info)).perform(click());

        navigateUp();

        onView(withId(R.id.button_info)).perform(click());

        pressBack();

        onView(withId(R.id.button_info)).perform(click());

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.legalInformation))).perform(click());

        pressBack();

        navigateUp();

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.appName)))
                .check(matches(isDisplayed()));

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.a_water_quality_testing_solution)))
                .check(matches(isDisplayed()));

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.test_water_quality_using)))
                .check(matches(isDisplayed()));

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.next)))
                .perform(click());

        onView(withId(R.id.button_info)).perform(click());

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.legalInformation))).perform(click());

        onView(withId(R.id.homeButton)).perform(click());

        navigateUp();

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.connect_with_app))).check(matches(isDisplayed()));

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.app_is_integrated)))
                .check(matches(isDisplayed()));

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.to_conduct_a_water_quality_test)))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_info)).perform(click());

        enterDiagnosticMode();

        onView(withId(R.id.actionSettings)).perform(click());

        pressBack();

        leaveDiagnosticMode();

        pressBack();

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.go_to_external_app))).perform(click());

        sleep(2000);

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text("Unnamed data point")));
    }

    @Test
    public void launchExternalApp() {

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.next)))
                .perform(click());

        onView(withText(TestHelper.getString(mActivityTestRule.getActivity(),
                R.string.go_to_external_app))).perform(click());

        sleep(2000);

        mDevice.waitForIdle();

        assertNotNull(mDevice.findObject(By.text("Unnamed data point")));

    }

    private void navigateUp() {
        ViewInteraction imageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        imageButton.perform(click());
    }
}
