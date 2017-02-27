package com.avseredyuk.securereco.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

/**
 * Created by lenfer on 2/26/17.
 */
public class PlayButtonClickListener implements View.OnClickListener {
    private Context context;

    public PlayButtonClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        final Call call = (Call) v.getTag();

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.password_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String password = userInput.getText().toString();
                                if (CallDao.getInstance().play(call, password)) {
                                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
