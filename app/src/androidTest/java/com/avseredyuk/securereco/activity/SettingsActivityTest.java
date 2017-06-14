package com.avseredyuk.securereco.activity;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.avseredyuk.securereco.application.Application;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Anton_Serediuk on 6/14/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {
    @Rule
    public IntentsTestRule<SettingsActivity> mActivityTestRule = new IntentsTestRule<>(SettingsActivity.class);

    @Before
    public void setUp() {
        ((Application) mActivityTestRule.getActivity().getApplication()).eraseAuthMan();
    }

    @Test
    public void tmp() {

    }


}
