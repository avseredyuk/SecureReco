package com.avseredyuk.securereco.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.avseredyuk.securereco.receiver.PhonecallReceiver;
import com.avseredyuk.securereco.util.Constant;

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
        filter.addAction(Constant.INTENT_START_RECORD);
        filter.addAction(Constant.INTENT_CANCEL_START_RECORD_NOTIFICATION);
        phoneCallReceiver = new PhonecallReceiver();
        this.registerReceiver(phoneCallReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }
}
