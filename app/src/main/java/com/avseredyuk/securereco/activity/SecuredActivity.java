package com.avseredyuk.securereco.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.util.Constant;

/**
 * Created by Anton_Serediuk on 6/7/2017.
 */

public abstract class SecuredActivity extends AppCompatActivity {
    private IntentFilter filter;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new ResetAuthenticationOnTimeoutBroadcastReceiver();
        filter = new IntentFilter(Constant.INTENT_BROADCAST_RESET_AUTH);
    }

    public void updateActionBarColors() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            int color;
            if (Application.getInstance().isAuthenticated()) {
                color = R.color.colorAuthenticated;
            } else {
                color = R.color.colorPrimary;
            }
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(
                            getResources().getColor(color)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.registerReceiver(receiver, filter);

        if (!Application.getInstance().authHolder.tryLock()) {
            Log.e("LOCK","SecuredActivity.onResume() on resume can't lock");
        }

        updateActionBarColors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        Application.getInstance().authHolder.unlock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Application.getInstance().getResetAuthStrategy()
                .equals(ResetAuthenticationStrategy.WHEN_APP_GOES_TO_BACKGROUND)) {
            if (!Application.getInstance().authHolder.isLocked()) {
                Application.getInstance().eraseAuthMan();
            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (Application.getInstance().getResetAuthStrategy()
                .equals(ResetAuthenticationStrategy.ON_TIMEOUT_OF_INACTIVITY)) {
            Application.getInstance().resetDisconnectTimer();
        }
    }

    public static class ResetAuthenticationOnTimeoutBroadcastReceiver extends BroadcastReceiver {
        public ResetAuthenticationOnTimeoutBroadcastReceiver() {
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context instanceof SecuredActivity) {
                ((SecuredActivity) context).updateOnAuthenticationStatusChange();
            }
        }
    }

    public abstract void updateOnAuthenticationStatusChange();

}
