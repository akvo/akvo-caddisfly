package org.akvo.caddisfly.test;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class StriptestTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityRule);

        resetLanguage();
    }

    @Test
    @RequiresDevice
    public void startStriptest() {

        activateTestMode();

        test5in1(false);
        testSoilNitrogen(false);
        testMerckPH(false);
        testNitrate100();

        test5in1(true);
        testSoilNitrogen(true);
        testMerckPH(true);
    }

    @After
    public void tearDown() {
        clearPreferences(mActivityRule);
    }

    private void test5in1(boolean external) {

        if (external) {

            gotoSurveyForm();

            TestUtil.nextSurveyPage(3);

            clickExternalSourceButton(3);

            mDevice.waitForIdle();

        } else {

            goToMainScreen();

            onView(withText(R.string.stripTest)).perform(click());

            onView(withText("Water - Chlorine, Hardness, Alkalinity, pH")).perform(click());
        }

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(8000);

        onView(withText("Start")).perform(click());

        sleep(36000);

        onView(withText("Start")).perform(click());

        sleep(35000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("Total Chlorine")).check(matches(isDisplayed()));
        onView(withText("0.00 mg/l")).check(matches(isDisplayed()));
        onView(withText("Free Chlorine")).check(matches(isDisplayed()));
        onView(withText("0.15 mg/l")).check(matches(isDisplayed()));
        onView(withText("Total Hardness")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));

        ViewInteraction textView11 = onView(
                allOf(withId(R.id.text_result), withText("No Result"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView11.check(matches(withText("No Result")));

//        onView(withText("No Result")).check(matches(isDisplayed()));
        onView(withText("Total Alkalinity")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withText("32.0 mg/l")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withText("pH")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));

        ViewInteraction textView12 = onView(
                allOf(withId(R.id.text_result), withText("No Result"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView12.check(matches(withText("No Result")));

//        onView(withText("No Result")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());

        if (external) {
            assertNotNull(mDevice.findObject(By.text("Total Chlorine: 0.0 mg/l")));
            assertNotNull(mDevice.findObject(By.text("Free Chlorine: 0.15 mg/l")));
            assertNotNull(mDevice.findObject(By.text("Total Hardness:  gpg")));
            assertNotNull(mDevice.findObject(By.text("Total Alkalinity: 32.0 mg/l")));
            assertNotNull(mDevice.findObject(By.text("pH: 6.2 ")));
        }

    }

    private void testSoilNitrogen(boolean external) {

        if (external) {

            gotoSurveyForm();

            TestUtil.nextSurveyPage(3);

            clickExternalSourceButton(2);

            mDevice.waitForIdle();

        } else {

            goToMainScreen();

            onView(withText(R.string.stripTest)).perform(click());

            onView(withText("Soil - Nitrogen")).perform(click());
        }

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(8000);

        onView(withText("Start")).perform(click());

        sleep(60000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("Nitrogen")).check(matches(isDisplayed()));
        onView(withText("205.1 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrate Nitrogen")).check(matches(isDisplayed()));
        onView(withText("41.0 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrite Nitrogen")).check(matches(isDisplayed()));
        onView(withText("0.01 mg/l")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());

        if (external) {
            assertNotNull(mDevice.findObject(By.text("Nitrogen: 205.075 mg/l")));
            assertNotNull(mDevice.findObject(By.text("Nitrate Nitrogen: 41.0 mg/l")));
            assertNotNull(mDevice.findObject(By.text("Nitrite Nitrogen: 0.01 mg/l")));
        }
    }

    private void testMerckPH(boolean external) {

        if (external) {

            gotoSurveyForm();

            TestUtil.nextSurveyPage(3);

            clickExternalSourceButton(1);

            mDevice.waitForIdle();

        } else {

            goToMainScreen();

            onView(withText(R.string.stripTest)).perform(click());

            onView(withText("Soil - pH (0 - 14)")).perform(click());
        }

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(8000);

        onView(withText("Start")).perform(click());

        sleep(5000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("pH")).check(matches(isDisplayed()));
        onView(withText("4.7")).check(matches(isDisplayed()));

        onView(withId(R.id.image_result)).check(matches(isDisplayed()));

        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());

        if (external) {
            assertNotNull(mDevice.findObject(By.text("pH: 4.7 ")));
        }
    }

    private void testNitrate100() {
        goToMainScreen();

        onView(withText(R.string.stripTest)).perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_types),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(11, click()));

        sleep(1000);

        onView(withText("Prepare for test")).perform(click());

        sleep(8000);

        onView(withText("Start")).perform(click());

        sleep(60000);

        onView(withText("Result")).check(matches(isDisplayed()));
        onView(withText("Nitrate")).check(matches(isDisplayed()));
        onView(withText("14.5 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrite")).check(matches(isDisplayed()));
        onView(withText("1.9 mg/l")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));

        onView(withText("Save")).perform(click());
    }
}
