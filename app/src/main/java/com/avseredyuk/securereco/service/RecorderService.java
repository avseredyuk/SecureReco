package com.avseredyuk.securereco.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.avseredyuk.securereco.receiver.PhonecallReceiver;

/**
 * Created by lenfer on 2/11/17.
 */
public class RecorderService extends Service {
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private BroadcastReceiver phoneCallReciever;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");
        super.onDestroy();
        this.unregisterReceiver(phoneCallReciever);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StartService", "RecorderService");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        phoneCallReciever = new PhonecallReceiver();
        this.registerReceiver(phoneCallReciever, filter);
        return super.onStartCommand(intent, flags, startId);
    }



}
