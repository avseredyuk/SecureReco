package com.avseredyuk.securereco.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

import java.util.List;

/**
 * Created by Anton_Serediuk on 5/12/2017.
 */

public class RegenerateKeysIntentService extends IntentService {
    public static volatile boolean isRunning;
    private Handler handler;

    public RegenerateKeysIntentService() {
        super("RegenerateKeysIntentService");
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isRunning = true;

        if (((Application) getApplicationContext()).isAuthenticated()) {
            AuthenticationManager authMan = ((Application) getApplicationContext()).getAuthMan();

            CallDao callDao = CallDao.getInstance();
            List<Call> calls = callDao.findAll();
            
            Call call = calls.get(0);
            callDao.reEncryptHeader(call, authMan);
            System.out.println(call.getFilename());

            //TODO work with ALL CALLS in loop
        /*
        for (Call call : calls) {
            System.out.println(call.getFilename());
            callDao.reEncryptHeader(call, );
        }
        */

            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_finished)));
        } else {
            Log.e(this.getClass().getSimpleName(),
                    "Non-authenticated at RegenerateKeysIntentService.onHandleIntent()");
            handler.post(new DisplayToast(this, getString(R.string.toast_please_authenticate_first)));
        }


        isRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("IS CREATED");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("IS DESTROYED");
    }

    public class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;
        public DisplayToast(Context mContext, String text){
            this.mContext = mContext;
            mText = text;
        }
        public void run(){
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
