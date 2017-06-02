package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.service.RegenerateKeysIntentService;
import com.avseredyuk.securereco.util.Constant;

/**
 * Created by lenfer on 3/1/17.
 */
public class SettingsActivity extends AppCompatActivity {
    private Context context;
    private EditText currentPasswordEdit;
    private Button regenerateRSAKeysButton;
    private EditText oldPasswordEdit;
    private EditText newPasswordEdit1;
    private EditText newPasswordEdit2;
    private Button changePasswordButton;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_settings);

        oldPasswordEdit = (EditText) findViewById(R.id.oldPasswordEdit);
        newPasswordEdit1 = (EditText) findViewById(R.id.newPasswordEdit1);
        newPasswordEdit2 = (EditText) findViewById(R.id.newPasswordEdit2);
        currentPasswordEdit = (EditText) findViewById(R.id.regenCurrentPasswordEdit);
        regenerateRSAKeysButton = (Button) findViewById(R.id.regenButton);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(new ChangePasswordButtonClickListener());
        regenerateRSAKeysButton.setOnClickListener(new RegenerateRSAKeysButtonClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Application.getInstance().authHolder.tryLock()) {
            Log.e("LOCK","SettingsActivity.onResume() on resume can't lock");
        }

        if (RegenerateKeysIntentService.isRunning) {
            currentPasswordEdit.setEnabled(false);
            regenerateRSAKeysButton.setEnabled(false);
        } else {
            currentPasswordEdit.setEnabled(true);
            regenerateRSAKeysButton.setEnabled(true);
        }

        if (Application.getInstance().isAuthenticated()) {
            oldPasswordEdit.setText(Constant.PASSWORD_FILLER);
            oldPasswordEdit.setEnabled(false);
        } else {
            oldPasswordEdit.setText("");
            oldPasswordEdit.setEnabled(true);
        }

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
    protected void onPause() {
        super.onPause();
        Application.getInstance().authHolder.unlock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Application.getInstance().authHolder.isLocked()) {
            Application.getInstance().setAuthMan(null);
        }
    }

    private class ChangePasswordButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AuthenticationManager authMan;

            String newPassword1 = newPasswordEdit1.getText().toString();
            String newPassword2 = newPasswordEdit2.getText().toString();
            if (newPassword1.length() == 0 ||
                    newPassword2.length() == 0 ||
                    !newPassword1.equals(newPassword2)) {
                Toast.makeText(context,
                        getString(R.string.toast_invalid_input),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (Application.getInstance().isAuthenticated()) {
                authMan = Application.getInstance().getAuthMan();
            } else {
                String oldPassword = oldPasswordEdit.getText().toString();
                if (oldPassword.length() > 0) {
                    try {
                        authMan = AuthenticationManager
                                .newAuthManWithAuthentication(oldPassword)
                                .setAsApplicationAuthenticationManager();
                    } catch (AuthenticationException e) {
                        Log.e(this.getClass().getSimpleName(),
                                "Error during authentication at ChangePasswordButtonClickListener", e);
                        Toast.makeText(getApplication(),
                                getString(R.string.toast_auth_error),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(context,
                            getString(R.string.toast_invalid_input),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (authMan.changePassword(newPassword1)) {
                Toast.makeText(context,
                        getString(R.string.toast_password_changed),
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(context,
                        getString(R.string.toast_wrong_password),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RegenerateRSAKeysButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AuthenticationManager authMan;

            String currentPassword = currentPasswordEdit.getText().toString();
            if (currentPassword.length() > 0) {
                if (Application.getInstance().isAuthenticated()) {
                    authMan = Application.getInstance().getAuthMan();
                } else {
                    try {
                        authMan = AuthenticationManager
                                .newAuthManWithAuthentication(currentPassword)
                                .setAsApplicationAuthenticationManager();
                    } catch (AuthenticationException e) {
                        Log.e(this.getClass().getSimpleName(),
                                "Error during authentication at RegenerateRSAKeysButtonClickListener", e);
                        Toast.makeText(getApplication(),
                                getString(R.string.toast_auth_error),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (authMan.regenerateKeyPair(context, currentPassword)) {
                    Toast.makeText(context,
                            getString(R.string.toast_keys_regen_started),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(context,
                            getString(R.string.toast_wrong_password),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context,
                        getString(R.string.toast_invalid_input),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
