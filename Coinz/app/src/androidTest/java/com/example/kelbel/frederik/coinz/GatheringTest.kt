package com.example.kelbel.frederik.coinz


import android.app.Activity
import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class GatheringTest {
    //This test uses the map from the 24.11.2018, testcase1 account
    //(It is recommended to not use the testcase1 or testcase2 for regular usage, although it should not mess up any tests)
    /* 1. This tests starts the app,
       2. logs into the testcase1 account,
       3. generates 1 coin of each currency and sets gold to 100000 for test purposes,
       4. makes all zones over the campus blue for test purposes,
       5. goes to the map tab via bottom navigation,
       6. starts mocking the location of the user and gathers one coin item of each currency,
       7. checks if the coins were collected by checking the counts in the wallet displayed*/

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION")!!

    private fun getCurrentActivity(): Activity {
        val currentActivity = arrayOfNulls<Activity>(1)
        getInstrumentation().runOnMainSync {
            val resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val it = resumedActivity.iterator()
            currentActivity[0] = it.next()
        }

        return currentActivity[0]!!
    }

    @Before
    fun grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(InstrumentationRegistry.getInstrumentation().uiAutomation) {
                executeShellCommand("appops set " + InstrumentationRegistry.getTargetContext().packageName + " android:mock_location allow")
                Thread.sleep(1000)
            }
        }
    }

    @Before
    fun flipTestSwitch() {
        ProfileActivity.isTest = true
    }

    @Test
    fun gatheringTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatEditText = onView(
                allOf(withId(R.id.enter_username),
                        isDisplayed()))
        appCompatEditText.perform(click())

        val appCompatEditText2 = onView(
                allOf(withId(R.id.enter_username),
                        isDisplayed()))
        appCompatEditText2.perform(replaceText("testc"), closeSoftKeyboard())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val appCompatEditText3 = onView(
                allOf(withId(R.id.enter_username), withText("testc"),
                        isDisplayed()))
        appCompatEditText3.perform(replaceText("testcase1"))

        val appCompatEditText4 = onView(
                allOf(withId(R.id.enter_username), withText("testcase1"),
                        isDisplayed()))
        appCompatEditText4.perform(closeSoftKeyboard())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000)

        val appCompatEditText5 = onView(
                allOf(withId(R.id.enter_username), withText("testcase1"),
                        isDisplayed()))
        appCompatEditText5.perform(pressImeActionButton())

        val appCompatEditText6 = onView(
                allOf(withId(R.id.enter_password),
                        isDisplayed()))
        appCompatEditText6.perform(replaceText("testcase1"), closeSoftKeyboard())

        val appCompatEditText7 = onView(
                allOf(withId(R.id.enter_password), withText("testcase1"),
                        isDisplayed()))
        appCompatEditText7.perform(pressImeActionButton())

        val appCompatButton = onView(
                allOf(withId(R.id.log_in_button), withText("Log in"),
                        isDisplayed()))
        appCompatButton.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val activity = getCurrentActivity()
        val b = activity is ProfileActivity

        if (b) {
            (activity as ProfileActivity).makeAllZonesBlue()
        }

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(7000)

        val bottomNavigationItemView = onView(
                allOf(withId(R.id.map_tab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_bar),
                                        0),
                                1),
                        isDisplayed()))
        bottomNavigationItemView.perform(click())

        if (b) {
            val fragmentMap = ((activity as ProfileActivity).supportFragmentManager.findFragmentByTag("B") as FragmentMap)
            for (i in 0..71) {
                activity.runOnUiThread {
                    if (b) {
                        fragmentMap.loc()
                    }
                }
                try {
                    Thread.sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        try {
            Thread.sleep(7000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val textView = onView(
                allOf(withId(R.id.shil_textview),
                        isDisplayed()))
        textView.check(matches(withText("2")))

        val textView2 = onView(
                allOf(withId(R.id.dolr_textview),
                        isDisplayed()))
        textView2.check(matches(withText("2")))

        val textView3 = onView(
                allOf(withId(R.id.quid_textview),
                        isDisplayed()))
        textView3.check(matches(withText("2")))

        val textView4 = onView(
                allOf(withId(R.id.peny_textview),
                        isDisplayed()))
        textView4.check(matches(withText("2")))

    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
