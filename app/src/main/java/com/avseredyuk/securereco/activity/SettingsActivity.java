package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.auth.AuthenticationManager;

/**
 * Created by lenfer on 3/1/17.
 */
public class SettingsActivity extends AppCompatActivity {
    private Context context;

    //todo: refactor this trash
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_settings);

        final EditText oldPasswordEdit = (EditText) findViewById(R.id.oldPasswordEdit);
        final EditText newPasswordEdit1 = (EditText) findViewById(R.id.newPasswordEdit1);
        final EditText newPasswordEdit2 = (EditText) findViewById(R.id.newPasswordEdit2);

        Button changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordEdit.getText().toString();
                String newPassword1 = newPasswordEdit1.getText().toString();
                String newPassword2 = newPasswordEdit2.getText().toString();
                if (oldPassword.length() > 0 &&
                        newPassword1.length() > 0 &&
                        newPassword2.length() > 0 &&
                        newPassword1.equals(newPassword2)) {
                    AuthenticationManager authMan = new AuthenticationManager();
                    if (authMan.changePassword(oldPassword, newPassword1)) {
                        Toast.makeText(context,
                                getString(R.string.toast_password_changed),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(context,
                                getString(R.string.toast_wrong_old_password),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context,
                            getString(R.string.toast_invalid_input),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        final EditText currentPasswordEdit = (EditText) findViewById(R.id.regenCurrentPasswordEdit);
        //todo: check whether asynctask is already running
        Button regenerateRSAKeysButton = (Button) findViewById(R.id.regenButton);
        regenerateRSAKeysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPasswordEdit.getText().toString();
                if (currentPassword.length() > 0) {
                    AuthenticationManager authMan = new AuthenticationManager();
                    if (authMan.regenerateKeyPair(currentPassword, context)) {
                        Toast.makeText(context,
                                getString(R.string.toast_keys_regen_started),
                                Toast.LENGTH_SHORT).show();
                        //todo: disable this button
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
        });

        System.out.println("CREATED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUMED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("PAUSED");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DESTROYED");
    }
}
