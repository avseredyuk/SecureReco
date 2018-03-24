package com.avseredyuk.securereco.application;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.configuration.Configuration;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.util.ArrayUtil;
import com.avseredyuk.securereco.util.ImageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static com.avseredyuk.securereco.util.Constant.INTENT_BROADCAST_RESET_AUTH;
import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_DELAY;

/**
 * Created by lenfer on 3/1/17.
 */
public class Application extends android.app.Application {
    private static final Handler disconnectHandler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private static final Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            if (Application.getInstance().getConfiguration().getResetAuthenticationStrategy()
                    .equals(ResetAuthenticationStrategy.ON_TIMEOUT_OF_INACTIVITY)) {
                Application.getInstance().eraseAuthMan();
                Application.getInstance().sendBroadcast(new Intent(INTENT_BROADCAST_RESET_AUTH));
            }
        }
    };
    private static Application instance;
    private final Map<String, Bitmap> contactPhotoCache = new HashMap<>();
    private final Map<String, String> contactNameCache = new HashMap<>();
    private Configuration configuration;
    private AuthenticationManager authMan = null;
    public final ReentrantLock authHolder = new ReentrantLock();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        contactPhotoCache.put(null,
                ImageUtil.getCircleCroppedBitmap(
                        ImageUtil.drawableToBitmap(getResources().getDrawable(R.drawable.ic_person_outline_black_24dp))
                )
        );
        configuration = new Configuration(getApplicationContext());
    }

    public void resetDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, RESET_AUTH_DELAY);
    }

    public Map<String, Bitmap> getContactPhotoCache() {
        return contactPhotoCache;
    }

    public Map<String, String> getContactNameCache() {
        return contactNameCache;
    }

    public void setAuthMan(AuthenticationManager paramAuthMan) {
        this.authMan = paramAuthMan;
    }

    public void eraseAuthMan() {
        if ((isAuthenticated())) {
            ArrayUtil.eraseArray(this.authMan.getPrivateKey());
        }
        this.authMan = null;
    }

    public AuthenticationManager getAuthMan() {
        return authMan;
    }

    public boolean isAuthenticated() {
        return authMan != null;
    }

    public static Application getInstance() {
        return instance;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
