package com.avseredyuk.securereco.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.avseredyuk.securereco.service.RecorderService;
import com.avseredyuk.securereco.util.ConfigUtil;

/**
 * Created by lenfer on 3/1/17.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Class<? extends Activity> activityClass;
        if (ConfigUtil.isConfigValid()) {
            activityClass = MainActivity.class;
        } else {
            activityClass = FirstRunActivity.class;
        }
        startActivity(new Intent(this, activityClass));

        startService(new Intent(this, RecorderService.class));

        finish();
    }
}
