package com.avseredyuk.securereco.activity;

import android.os.SystemClock;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityMenuTest {
    public static final String TYPED_PASSWORD = "123";

    @Rule
    public IntentsTestRule<MainActivity> mActivityTestRule = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        ((Application) mActivityTestRule.getActivity().getApplication()).eraseAuthMan();
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

    @Test
    public void testSettingsActivityAppear() {
        onView(withText(R.string.menu_item_settings)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    @Test
    public void testAuthDialogPressOkOnValidPassword() {
        onView(withText(R.string.menu_item_authenticate)).perform(click());
        onView(withId(R.id.passwordPromptInputPasswordEditText)).perform(typeText(TYPED_PASSWORD));
        onView(withText(R.string.password_dialog_button_ok)).perform(click());
        onView(withText(R.string.toast_authenticated))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
        SystemClock.sleep(2000);
    }

    @Test
    public void testChangeOnAuthMenuItemAfterSuccessfulAuth() {
        onView(withText(R.string.menu_item_authenticate)).perform(click());
        onView(withId(R.id.passwordPromptInputPasswordEditText)).perform(typeText(TYPED_PASSWORD));
        onView(withText(R.string.password_dialog_button_ok)).perform(click());
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.menu_item_deauthenticate))
                .check(matches(isDisplayed()));
    }


}
