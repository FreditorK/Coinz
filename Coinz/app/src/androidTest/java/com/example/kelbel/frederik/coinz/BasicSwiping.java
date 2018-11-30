package com.example.kelbel.frederik.coinz;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BasicSwiping {
    //This test uses the testcase1 account
    //(It is recommended to not use the testcase1 or testcase2 for regular usage, although it should not mess up any tests)
    /* 1. This tests starts the app,
       2. logs into the testcase1 account,
       3. Tests extensive swiping between bottom-navigation fragments and viewpager fragments*/

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void basicSwiping() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.enter_username),
                        isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.enter_username),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("testc"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.enter_username), withText("testc"),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("testcase1"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.enter_username), withText("testcase1"),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.enter_username), withText("testcase1"),
                        isDisplayed()));
        appCompatEditText5.perform(pressImeActionButton());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.enter_password),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText("testcase1"), closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.enter_password), withText("testcase1"),
                        isDisplayed()));
        appCompatEditText7.perform(pressImeActionButton());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.log_in_button), withText("Log in"),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.map_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.settings_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView2.perform(click());

        ViewInteraction bottomNavigationItemView3 = onView(
                allOf(withId(R.id.depot_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView3.perform(click());

        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout_id),
                                0),
                        1),
                        isDisplayed()));
        tabView.perform(click());

        ViewInteraction tabView2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout_id),
                                0),
                        3),
                        isDisplayed()));
        tabView2.perform(click());

        ViewInteraction tabView3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout_id),
                                0),
                        2),
                        isDisplayed()));
        tabView3.perform(click());

        ViewInteraction tabView4 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout_id),
                                0),
                        1),
                        isDisplayed()));
        tabView4.perform(click());

        ViewInteraction bottomNavigationItemView4 = onView(
                allOf(withId(R.id.map_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView4.perform(click());

        ViewInteraction bottomNavigationItemView5 = onView(
                allOf(withId(R.id.settings_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView5.perform(click());

        ViewInteraction bottomNavigationItemView6 = onView(
                allOf(withId(R.id.depot_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView6.perform(click());

        ViewInteraction tabView5 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.tabLayout_id),
                                0),
                        2),
                        isDisplayed()));
        tabView5.perform(click());

        ViewInteraction bottomNavigationItemView7 = onView(
                allOf(withId(R.id.map_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView7.perform(click());

        ViewInteraction bottomNavigationItemView8 = onView(
                allOf(withId(R.id.depot_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView8.perform(click());
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
}
