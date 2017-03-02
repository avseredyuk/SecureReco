package com.avseredyuk.securereco.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.util.ConfigUtil;

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
                if (input.getText().length() == 0) {
                    Toast msg = Toast.makeText(getBaseContext(), "Please enter password",
                            Toast.LENGTH_LONG);
                    msg.show();
                } else {
                    String password = input.getText().toString();
                    ConfigUtil.makeKeys(password);
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
