package com.avseredyuk.securereco.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.callback.Callback;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.util.Constant;

/**
 * Created by Anton_Serediuk on 6/7/2017.
 */

public abstract class SecuredActivity extends AppCompatActivity {
    private IntentFilter resetAuthOnTimeoutFilter;
    private BroadcastReceiver resetAuthOnTimeoutReceiver;

    public void updateUIOnAuthenticationReset() {
        updateActionBarColors();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetAuthOnTimeoutReceiver = new ResetAuthenticationOnTimeoutBroadcastReceiver();
        resetAuthOnTimeoutFilter = new IntentFilter(Constant.INTENT_BROADCAST_RESET_AUTH);
    }

    protected void makeAlertDialog(final Callback callback) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.password_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.password_dialog_button_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String password = userInput.getText().toString();
                                try {
                                    AuthenticationManager
                                            .newAuthManWithAuthentication(password)
                                            .setAsApplicationAuthenticationManager();

                                    updateActionBarColors();

                                    if (callback != null) {
                                        callback.execute(password);
                                    }

                                    Toast.makeText(getApplication(),
                                            getString(R.string.toast_authenticated),
                                            Toast.LENGTH_SHORT).show();
                                } catch (AuthenticationException e) {
                                    Log.e(this.getClass().getSimpleName(),
                                            "Error during authentication at SecuredActivity", e);
                                    Toast.makeText(getApplication(),
                                            getString(R.string.toast_auth_error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.password_dialog_button_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
        super.registerReceiver(resetAuthOnTimeoutReceiver, resetAuthOnTimeoutFilter);

        if (!Application.getInstance().authHolder.tryLock()) {
            Log.e("LOCK","SecuredActivity.onResume() on resume can't lock");
        }

        updateActionBarColors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(resetAuthOnTimeoutReceiver);
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
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context instanceof SecuredActivity) {
                ((SecuredActivity) context).updateUIOnAuthenticationReset();
            }
        }
    }

}
