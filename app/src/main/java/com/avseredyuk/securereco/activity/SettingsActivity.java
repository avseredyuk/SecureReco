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
                        Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(context, "Wrong old password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error in input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final EditText currentPasswordEdit = (EditText) findViewById(R.id.regenCurrentPasswordEdit);
        Button regenerateRSAKeysButton = (Button) findViewById(R.id.regenButton);
        regenerateRSAKeysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPasswordEdit.getText().toString();
                if (currentPassword.length() > 0) {
                    AuthenticationManager authMan = new AuthenticationManager();
                    if (authMan.regenerateKeyPair(currentPassword)) {
                        Toast.makeText(context, "Keys regenerated", Toast.LENGTH_SHORT).show();
                        //finish();
                    } else {
                        Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
