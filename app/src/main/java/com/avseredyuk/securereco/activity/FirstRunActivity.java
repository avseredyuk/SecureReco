package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.util.ConfigUtil;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class FirstRunActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.getApplicationContext();

        setContentView(R.layout.activity_firstrun);
        Button buttonStart = (Button) findViewById(R.id.button);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.editText);
                if (input.getText().length() > 0) {
                    String password = input.getText().toString();

                    try {
                        AuthenticationManager
                                .newAuthManInitialKeyGenWithAuthentication(password)
                                .setAsApplicationAuthenticationManager(getApplicationContext());
                    } catch (AuthenticationException e) {
                        Log.e(this.getClass().getSimpleName(),
                                "Error during authentication at FirstRunActivity", e);
                        Toast.makeText(getApplication(),
                                getString(R.string.toast_error),
                                Toast.LENGTH_SHORT).show();
                    }

                    ConfigUtil.writeValue(NOTIFICATION_ON, Boolean.toString(true));

                    ConfigUtil.writeValue(IS_ENABLED, Boolean.toString(true));

                    startActivity(new Intent(context, MainActivity.class));
                } else {
                    Toast.makeText(context,
                            getString(R.string.toast_please_enter_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
