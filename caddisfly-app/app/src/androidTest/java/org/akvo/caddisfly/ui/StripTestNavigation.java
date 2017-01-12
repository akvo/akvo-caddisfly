package org.akvo.caddisfly.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StripTestNavigation {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @SuppressWarnings("SameParameterValue")
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

    @Test
    public void stripTestNav() {
        ViewInteraction appCompatButton = onView(
                allOf(ViewMatchers.withId(R.id.buttonStripTest), withText("Strip Test"),
                        withParent(withId(R.id.mainLayout)),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.list_types),
                        2),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.image_pageRight),
                        withParent(withId(R.id.layout_footer)),
                        isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction appCompatImageView2 = onView(
                allOf(withId(R.id.image_pageRight),
                        withParent(withId(R.id.layout_footer)),
                        isDisplayed()));
        appCompatImageView2.perform(click());

        ViewInteraction appCompatImageView3 = onView(
                allOf(withId(R.id.image_pageRight),
                        withParent(withId(R.id.layout_footer)),
                        isDisplayed()));
        appCompatImageView3.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

    }
}
