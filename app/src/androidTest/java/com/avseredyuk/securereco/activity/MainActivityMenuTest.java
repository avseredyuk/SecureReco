package com.avseredyuk.securereco.activity;

import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.avseredyuk.securereco.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityMenuTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    }

    @Test
    public void testAuthDialogAppear() {
        onView(withText(R.string.menu_item_authenticate)).perform(click());
        onView(withId(R.id.passwordPromptTextView))
                .inRoot(isDialog())
                .check(matches(withText(R.string.enter_password)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.passwordPromptInputPasswordEditText))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.password_dialog_button_ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.password_dialog_button_cancel))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAuthDialogPressOkOnEmptyPassword() {
        onView(withText(R.string.menu_item_authenticate)).perform(click());
        onView(withText(R.string.password_dialog_button_ok)).perform(click());
        onView(withText(R.string.toast_auth_error))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);
    }


}
