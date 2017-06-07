package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.service.RegenerateKeysIntentService;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.Constant;

import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_STRATEGY;

/**
 * Created by lenfer on 3/1/17.
 */
public class SettingsActivity extends SecuredActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private Spinner resetAuthStrategySpinner;
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

        resetAuthStrategySpinner = (Spinner) findViewById(R.id.resetAuthStrategySpinner);
        oldPasswordEdit = (EditText) findViewById(R.id.oldPasswordEdit);
        newPasswordEdit1 = (EditText) findViewById(R.id.newPasswordEdit1);
        newPasswordEdit2 = (EditText) findViewById(R.id.newPasswordEdit2);
        currentPasswordEdit = (EditText) findViewById(R.id.regenCurrentPasswordEdit);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        regenerateRSAKeysButton = (Button) findViewById(R.id.regenButton);

        resetAuthStrategySpinner.setOnItemSelectedListener(this);
        changePasswordButton.setOnClickListener(new ChangePasswordButtonClickListener());
        regenerateRSAKeysButton.setOnClickListener(new RegenerateRSAKeysButtonClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetAuthStrategySpinner.setSelection(ConfigUtil.readInt(RESET_AUTH_STRATEGY));

        if (RegenerateKeysIntentService.isRunning) {
            currentPasswordEdit.setEnabled(false);
            regenerateRSAKeysButton.setEnabled(false);
        } else {
            currentPasswordEdit.setEnabled(true);
            regenerateRSAKeysButton.setEnabled(true);
        }

        updateOnAuthenticationStatusChange();
    }

    @Override
    public void updateOnAuthenticationStatusChange() {
        if (Application.getInstance().isAuthenticated()) {
            oldPasswordEdit.setText(Constant.PASSWORD_FILLER);
            oldPasswordEdit.setEnabled(false);
        } else {
            oldPasswordEdit.setText("");
            oldPasswordEdit.setEnabled(true);
        }
        updateActionBarColors();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ConfigUtil.writeValue(RESET_AUTH_STRATEGY, String.valueOf(position));
        Application.getInstance().setResetAuthStrategy(
                ResetAuthenticationStrategy.valueOf(position)
        );
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // auto-generated method stub
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
