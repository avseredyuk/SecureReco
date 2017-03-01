package com.avseredyuk.securereco;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/**
 * Created by lenfer on 3/1/17.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SystemClock.sleep(TimeUnit.SECONDS.toMillis(3));
    }
}
