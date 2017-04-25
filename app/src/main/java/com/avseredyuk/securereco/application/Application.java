package com.avseredyuk.securereco.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.avseredyuk.securereco.R;

/**
 * Created by lenfer on 3/1/17.
 */
public class Application extends android.app.Application {
    private Map<String, Bitmap> contactPhotoCache = new HashMap<>();
    private Map<String, String> contactNameCache = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        contactPhotoCache.put(null, BitmapFactory.decodeResource(getResources(), R.drawable.avatar_unknown));
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));
    }

    public Map<String, Bitmap> getContactPhotoCache() {
        return contactPhotoCache;
    }

    public Map<String, String> getContactNameCache() {
        return contactNameCache;
    }
}
