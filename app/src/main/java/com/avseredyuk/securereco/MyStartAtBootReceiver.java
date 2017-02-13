package com.avseredyuk.securereco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by lenfer on 2/13/17.
 */
public class MyStartAtBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, TService.class);
            context.startService(serviceIntent);
        }
    }
}
