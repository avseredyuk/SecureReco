package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.callback.Callback;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.service.RegenerateKeysIntentService;
import com.avseredyuk.securereco.util.ConfigUtil;

import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_STRATEGY;

/**
 * Created by lenfer on 3/1/17.
 */
public class SettingsActivity extends SecuredActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private Spinner resetAuthStrategySpinner;
    private Button regenerateRSAKeysButton;
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
        newPasswordEdit1 = (EditText) findViewById(R.id.changePasswordNewPasswordEdit1);
        newPasswordEdit2 = (EditText) findViewById(R.id.changePasswordNewPasswordEdit2);
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
            regenerateRSAKeysButton.setEnabled(false);
        } else {
            regenerateRSAKeysButton.setEnabled(true);
        }

        updateUIOnAuthenticationReset();
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
        boolean isEditTextDataValid(EditText e1, EditText e2) {
            final String s1 = e1.getText().toString();
            final String s2 = e2.getText().toString();
            return (s1.length() != 0 && s2.length() != 0 && s1.equals(s2));
        }

        @Override
        public void onClick(View v) {
            if (!isEditTextDataValid(newPasswordEdit1, newPasswordEdit2)) {
                Toast.makeText(context,
                        getString(R.string.toast_invalid_input),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Callback changePasswordCallback = new Callback() {
                @Override
                public void execute(String password) {
                    if (Application.getInstance().getAuthMan().changePassword(newPasswordEdit1.getText().toString())) {
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
            };

            if (Application.getInstance().isAuthenticated()) {
                changePasswordCallback.execute(null);
            } else {
                makeAlertDialog(changePasswordCallback);
            }
        }
    }

    private class RegenerateRSAKeysButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Callback regenerateRSAKeysCallback = new Callback() {
                @Override
                public void execute(String password) {
                    if (Application.getInstance().getAuthMan().regenerateKeyPair(context, password)) {
                        Toast.makeText(context,
                                getString(R.string.toast_keys_regen_changed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                getString(R.string.toast_wrong_password),
                                Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            };

            // Here we have to ask for password no mather our authentication status
            // because of the fact that regenerate keys procedure requires current password
            makeAlertDialog(regenerateRSAKeysCallback);
        }
    }
}
