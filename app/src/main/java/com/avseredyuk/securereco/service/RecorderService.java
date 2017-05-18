package com.avseredyuk.securereco.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.avseredyuk.securereco.receiver.PhonecallReceiver;

/**
 * Created by lenfer on 2/11/17.
 */
public class RecorderService extends Service {
    private BroadcastReceiver phoneCallReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(phoneCallReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        phoneCallReceiver = new PhonecallReceiver();
        this.registerReceiver(phoneCallReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }



}
