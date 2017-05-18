package com.avseredyuk.securereco.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.util.ArrayUtil;

import static com.avseredyuk.securereco.util.Constant.SPLASH_SHOW_TIME_IN_SECONDS;

/**
 * Created by lenfer on 3/1/17.
 */
public class Application extends android.app.Application {
    private Map<String, Bitmap> contactPhotoCache = new HashMap<>();
    private Map<String, String> contactNameCache = new HashMap<>();
    private AuthenticationManager authMan = null;
    public ReentrantLock authHolder = new ReentrantLock();

    @Override
    public void onCreate() {
        super.onCreate();
        contactPhotoCache.put(null, BitmapFactory.decodeResource(getResources(), R.drawable.avatar_unknown));
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(SPLASH_SHOW_TIME_IN_SECONDS));
    }

    public Map<String, Bitmap> getContactPhotoCache() {
        return contactPhotoCache;
    }

    public Map<String, String> getContactNameCache() {
        return contactNameCache;
    }

    public void setAuthMan(AuthenticationManager authMan) {
        if (authMan == null) {
            if (isAuthenticated()) {
                ArrayUtil.eraseArray(this.authMan.getPrivateKey());
            }
        }
        this.authMan = authMan;
    }

    public AuthenticationManager getAuthMan() {
        return authMan;
    }

    public boolean isAuthenticated() {
        return authMan != null;
    }
}
