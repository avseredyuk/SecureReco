package com.avseredyuk.securereco.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.avseredyuk.securereco.R;

/**
 * Created by Anton_Serediuk on 5/12/2017.
 */

public class RegenerateKeysIntentService extends IntentService {
    public RegenerateKeysIntentService() {
        super("RegenerateKeysIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String msg = intent.getStringExtra("QQQ");
        SystemClock.sleep(5000); // 30 seconds
        String resultTxt = msg + " "
                + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());
        Toast.makeText(getApplicationContext(),
                resultTxt,
                Toast.LENGTH_SHORT).show();
    }
}
