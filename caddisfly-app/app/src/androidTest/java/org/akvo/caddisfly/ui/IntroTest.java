package org.akvo.caddisfly.ui;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextSurveyPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

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

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityTestRule);
    }

    @Test
    public void introTest() {

        String goToTest = getString(R.string.goToTest);

        onView(withId(R.id.button_info)).perform(click());

        navigateUp();

        onView(withId(R.id.button_info)).perform(click());

        pressBack();

        onView(withId(R.id.button_info)).perform(click());

        onView(withText(R.string.legalInformation)).perform(click());

        pressBack();

        navigateUp();

        onView(withText(R.string.appName)).check(matches(isDisplayed()));

        onView(withText(R.string.a_water_quality_testing_solution))
                .check(matches(isDisplayed()));

        onView(withText(R.string.test_water_quality_using))
                .check(matches(isDisplayed()));

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.button_info)).perform(click());

        onView(withText(R.string.legalInformation)).perform(click());

        onView(withId(R.id.homeButton)).perform(click());

        navigateUp();

        onView(withText(R.string.connect_with_app)).check(matches(isDisplayed()));

        onView(withText(R.string.app_is_integrated))
                .check(matches(isDisplayed()));

        onView(withText(R.string.to_conduct_a_water_quality_test))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_info)).perform(click());

        enterDiagnosticMode();

        onView(withId(R.id.actionSettings)).perform(click());

        pressBack();

        leaveDiagnosticMode();

        pressBack();

        onView(withText(R.string.go_to_external_app)).perform(click());

        sleep(500);

        mDevice.waitForIdle();

        gotoSurveyForm();

        mActivityTestRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text(goToTest)));
    }

    @Test
    public void launchExternalApp() {

        goToMainScreen();

        final String goToTest = getString(R.string.goToTest);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.go_to_external_app)).perform(click());

        sleep(500);

        mActivityTestRule.finishActivity();

        mDevice.waitForIdle();

        gotoSurveyForm();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text(goToTest)));
    }

    private void navigateUp() {
        ViewInteraction imageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
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
