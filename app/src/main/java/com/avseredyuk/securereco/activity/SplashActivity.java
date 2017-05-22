package com.avseredyuk.securereco.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.service.RecorderService;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.util.concurrent.TimeUnit;

import static com.avseredyuk.securereco.util.Constant.SPLASH_SHOW_TIME_IN_SECONDS;

/**
 * Created by lenfer on 3/1/17.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(Application.getInstance(), RecorderService.class));

        SystemClock.sleep(TimeUnit.SECONDS.toMillis(SPLASH_SHOW_TIME_IN_SECONDS));

        final Class<? extends Activity> activityClass;
        if (ConfigUtil.isConfigValid()) {
            activityClass = MainActivity.class;
        } else {
            activityClass = FirstRunActivity.class;
        }
        startActivity(new Intent(Application.getInstance(), activityClass));

        finish();
    }
}
