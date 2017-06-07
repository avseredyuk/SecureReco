package com.avseredyuk.securereco.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.avseredyuk.securereco.service.RecorderService;

/**
 * Created by lenfer on 2/13/17.
 */
public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, RecorderService.class);
            context.startService(serviceIntent);
        }
    }
}
