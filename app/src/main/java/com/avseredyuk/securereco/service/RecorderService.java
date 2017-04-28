package com.avseredyuk.securereco.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.avseredyuk.securereco.receiver.PhonecallReceiver;

import static com.avseredyuk.securereco.util.Constant.ANDROID_INTENT_ACTION_PHONE_STATE;
import static com.avseredyuk.securereco.util.Constant.ANDROID_INTENT_ACTION_NEW_OUTGOING_CALL;

/**
 * Created by lenfer on 2/11/17.
 */
public class RecorderService extends Service {
    private BroadcastReceiver phoneCallReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");
        super.onDestroy();
        this.unregisterReceiver(phoneCallReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StartService", "RecorderService");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ANDROID_INTENT_ACTION_NEW_OUTGOING_CALL);
        filter.addAction(ANDROID_INTENT_ACTION_PHONE_STATE);
        phoneCallReceiver = new PhonecallReceiver();
        this.registerReceiver(phoneCallReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }



}
