package org.akvo.caddisfly.instruction;


import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestConstant;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ManualInstruction {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
        prefs.edit().clear().apply();

        resetLanguage();
    }

    @Test
    public void instructionTest() {

        gotoSurveyForm();

        for (int i = 0; i < 5; i++) {
            clickExternalSourceButton(TestConstant.NEXT);
        }

        clickExternalSourceButton(2, TestConstant.GO_TO_TEST);

        SystemClock.sleep(6000);

        //onView(allOf(withId(R.id.textTitle), withText("Lovibond SD 50 pH me..."))).check(matches(isDisplayed()));

        onView(withText("Instructions")).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        Espresso.pressBack();

    }
}
