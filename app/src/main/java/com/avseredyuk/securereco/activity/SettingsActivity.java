package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.callback.Callback;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.service.BackgroundWorkIntentService;
import com.avseredyuk.securereco.util.AudioSourceEnum;
import com.avseredyuk.securereco.util.ConfigUtil;

import static com.avseredyuk.securereco.util.Constant.AUDIO_SOURCE;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_CHANGE_FOLDER;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_REGENERATE_KEYS;
import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_STRATEGY;

/**
 * Created by lenfer on 3/1/17.
 */
public class SettingsActivity extends SecuredActivity {
    private Context context;
    private Spinner resetAuthStrategySpinner;
    private Spinner audioSourceSpinner;
    private Button regenerateRSAKeysButton;
    private EditText newPasswordEdit1;
    private EditText newPasswordEdit2;
    private EditText changeFolderEdit;
    private Button changeFolderButton;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class ResetAuthSpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ConfigUtil.writeValue(RESET_AUTH_STRATEGY, String.valueOf(position));
            Application.getInstance().setResetAuthStrategy(
                    ResetAuthenticationStrategy.valueOf(position)
            );
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // nothing
        }
    }

    private class AudioSourceSpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String audioSource = parent.getItemAtPosition(position).toString();
            ConfigUtil.writeValue(AUDIO_SOURCE, audioSource);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // nothing
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_settings);

        resetAuthStrategySpinner = (Spinner) findViewById(R.id.resetAuthStrategySpinner);
        audioSourceSpinner = (Spinner) findViewById(R.id.audioSourceSpinner);
        newPasswordEdit1 = (EditText) findViewById(R.id.changePasswordNewPasswordEdit1);
        newPasswordEdit2 = (EditText) findViewById(R.id.changePasswordNewPasswordEdit2);
        Button changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        regenerateRSAKeysButton = (Button) findViewById(R.id.regenButton);
        changeFolderEdit = (EditText) findViewById(R.id.changeFolderEdit);
        changeFolderButton = (Button) findViewById(R.id.changeFolderButton);

        resetAuthStrategySpinner.setOnItemSelectedListener(new ResetAuthSpinnerItemSelectedListener());
        audioSourceSpinner.setOnItemSelectedListener(new AudioSourceSpinnerItemSelectedListener());
        changePasswordButton.setOnClickListener(new ChangePasswordButtonClickListener());
        regenerateRSAKeysButton.setOnClickListener(new RegenerateRSAKeysButtonClickListener());
        changeFolderButton.setOnClickListener(new ChangeFolderButtonClickListener());

        audioSourceSpinner.setAdapter
                (new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        AudioSourceEnum.values())
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetAuthStrategySpinner.setSelection(
                ConfigUtil.readInt(RESET_AUTH_STRATEGY)
        );

        audioSourceSpinner.setSelection(
                AudioSourceEnum.valueOf(
                        ConfigUtil.readValue(AUDIO_SOURCE)
                ).ordinal()
        );

        regenerateRSAKeysButton.setEnabled(
                !isBackgroundRunningAction(BWIS_DESTINATION_REGENERATE_KEYS)
        );

        changeFolderEdit.setText(ConfigUtil.getCallLogsDir());
        changeFolderEdit.setEnabled(
                !isBackgroundRunningAction(BWIS_DESTINATION_CHANGE_FOLDER)
        );

        changeFolderButton.setEnabled(
                !isBackgroundRunningAction(BWIS_DESTINATION_CHANGE_FOLDER)
        );

        updateUIOnAuthenticationReset();
    }

    private boolean isBackgroundRunningAction(String action) {
        if (BackgroundWorkIntentService.isRunning) {
            String localActionFromService = BackgroundWorkIntentService.action;
            return ((localActionFromService != null) && (action.equals(localActionFromService)));
        }
        return false;
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
                                getString(R.string.toast_error_changing_password),
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
                    Application.getInstance().getAuthMan().regenerateKeyPair(context, password);
                    Toast.makeText(context,
                            getString(R.string.toast_keys_regen_changed),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            };

            // Here we have to ask for password no matter our authentication status
            // because of the fact that regenerate keys procedure requires current password
            makeAlertDialog(regenerateRSAKeysCallback);
        }
    }

    private class ChangeFolderButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Callback changeFolderCallback = new Callback() {
                @Override
                public void execute(String password) {
                    Application.getInstance().getAuthMan().changeFolder(context, changeFolderEdit.getText().toString());
                    Toast.makeText(context,
                            getString(R.string.toast_calls_folder_changing),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            };
            if (Application.getInstance().isAuthenticated()) {
                changeFolderCallback.execute(null);
            } else {
                makeAlertDialog(changeFolderCallback);
            }
        }
    }
}
