package com.avseredyuk.securereco.activity;


import android.os.SystemClock;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.avseredyuk.securereco.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.avseredyuk.securereco.R.id.button;
import static junit.framework.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FirstRunActivityTest {
    public static final String TYPED_PASSWORD = "123";

    @Rule
    public IntentsTestRule<FirstRunActivity> mActivityTestRule = new IntentsTestRule<>(FirstRunActivity.class);

    @Test
    public void firstRunActivityTestEmptyPassword() {
        onView(withId(button)).perform(click());
        onView(withText(R.string.toast_please_enter_password))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);
    }

    @Test
    public void firstRunActivityTestValidPasswordStartMainActivity() {
        onView(withId(R.id.editText)).perform(typeText(TYPED_PASSWORD));
        onView(withId(button)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
        assertTrue(mActivityTestRule.getActivity().isFinishing());
    }

}
