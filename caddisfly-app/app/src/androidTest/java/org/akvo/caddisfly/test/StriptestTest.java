package org.akvo.caddisfly.test;

import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.activateTestMode;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.nextSurveyPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;

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

        //clearPreferences(mActivityRule);

    }

    @Test
    @RequiresDevice
    public void startStriptest() {

        activateTestMode();

        sleep(10000);

        testSoilNitrogen();

        testNitrate100();
        test5in1();
        testMerckPH();
        testMercury();
    }

    @After
    public void tearDown() {
        clearPreferences(mActivityRule);
    }

    private void test5in1() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Strip Tests");

        clickExternalSourceButton(3);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(8000);

        onView(withText(R.string.start)).perform(click());

        sleep(36000);

        onView(withText(R.string.start)).perform(click());

        sleep(35000);

        onView(withText(R.string.result)).check(matches(isDisplayed()));
        onView(withText("Total Chlorine")).check(matches(isDisplayed()));
        onView(withText("0 mg/l")).check(matches(isDisplayed()));
        onView(withText("Free Chlorine")).check(matches(isDisplayed()));
        onView(withText("0.15 mg/l")).check(matches(isDisplayed()));
        onView(withText("Total Hardness")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));

        mDevice.waitForIdle();
        mDevice.swipe(200, 750, 200, 600, 4);

        onView(withText(R.string.no_result)).check(matches(isDisplayed()));

        mDevice.waitForIdle();
        mDevice.swipe(200, 750, 200, 600, 4);

        onView(withText("Total Alkalinity")).check(matches(isDisplayed()));
        onView(withText("32 mg/l")).check(matches(isDisplayed()));

        mDevice.waitForIdle();
        mDevice.swipe(200, 750, 200, 600, 4);

        onView(withText("pH")).check(matches(isDisplayed()));
        onView(withText("6.2")).check(matches(isDisplayed()));

        onView(withText(R.string.save)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Total Chlorine: 0.0 mg/l")));
        assertNotNull(mDevice.findObject(By.text("Free Chlorine: 0.15 mg/l")));
        assertNotNull(mDevice.findObject(By.text("Total Hardness:  mg/l")));
        assertNotNull(mDevice.findObject(By.text("Total Alkalinity: 32.0 mg/l")));
        assertNotNull(mDevice.findObject(By.text("pH: 6.2 ")));
    }

    private void testSoilNitrogen() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Soil Striptest");

        clickExternalSourceButton(0);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(10000);

        onView(withText(R.string.start)).perform(click());

        sleep(65000);

        onView(withText(R.string.result)).check(matches(isDisplayed()));
        onView(withText("Nitrogen")).check(matches(isDisplayed()));
        onView(withText("205.15 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrate Nitrogen")).check(matches(isDisplayed()));
        onView(withText("41 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrite Nitrogen")).check(matches(isDisplayed()));
        onView(withText("0.03 mg/l")).check(matches(isDisplayed()));
        onView(withText(R.string.save)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Nitrogen: 205.15 mg/l")));
        assertNotNull(mDevice.findObject(By.text("Nitrate Nitrogen: 41.0 mg/l")));
        assertNotNull(mDevice.findObject(By.text("Nitrite Nitrogen: 0.03 mg/l")));
    }

    private void testMerckPH() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Strip Tests");

        clickExternalSourceButton(2);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(8000);

        onView(withText(R.string.start)).perform(click());

        sleep(5000);

        onView(withText(R.string.result)).check(matches(isDisplayed()));
        onView(withText("pH")).check(matches(isDisplayed()));
        onView(withText("4.8")).check(matches(isDisplayed()));

        onView(withId(R.id.image_result)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")));
    }

    private void testNitrate100() {

        gotoSurveyForm();

        nextSurveyPage("Strip Tests");

        clickExternalSourceButton(5);

        sleep(1000);

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(8000);

        onView(withText(R.string.start)).perform(click());

        sleep(60000);

        onView(withText(R.string.result)).check(matches(isDisplayed()));
        onView(withText("Nitrate")).check(matches(isDisplayed()));
        onView(withText("14.5 mg/l")).check(matches(isDisplayed()));
        onView(withText("Nitrite")).check(matches(isDisplayed()));
        onView(withText("1.85 mg/l")).check(matches(isDisplayed()));
        onView(withText(R.string.save)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).perform(click());
    }

    private void testMercury() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Strip Tests");

        UiScrollable listView = new UiScrollable(new UiSelector());
        try {
            listView.scrollToEnd(1);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        clickExternalSourceButton(2);

        mDevice.waitForIdle();

        sleep(1000);

        onView(withText(R.string.prepare_test)).perform(click());

        sleep(8000);

        onView(withText(R.string.start)).perform(click());

        sleep(35000);

        onView(withText(R.string.result)).check(matches(isDisplayed()));
        onView(withText("Mercury")).check(matches(isDisplayed()));
        onView(withText("0.005 mg/l")).check(matches(isDisplayed()));

        onView(withId(R.id.image_result)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).check(matches(isDisplayed()));

        onView(withText(R.string.save)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Mercury: 0.005 mg/l")));
    }
}
