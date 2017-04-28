package com.avseredyuk.securereco.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.adapter.CallArrayAdapter;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.io.File;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.IS_ENABLED;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ON;

public class MainActivity extends AppCompatActivity {
    private CallArrayAdapter callArrayAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        ListView callsListView = (ListView) findViewById(R.id.listView);
        List<Call>  calls = CallDao.getInstance().findAll(Call.CallDateComparator);
        callArrayAdapter = new CallArrayAdapter(this, calls);
        callsListView.setAdapter(callArrayAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            int color;
            if (((Application) getApplicationContext()).isAuthenticated()) {
                color = R.color.colorAuthenticated;
            } else {
                color = R.color.colorPrimary;
            }
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(
                            getResources().getColor(color)));
        }

        System.out.println("RESUMED");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteSelectedMenuItem = menu.findItem(R.id.action_delete_selected);
        MenuItem enabledDisabledMenuItem = menu.findItem(R.id.action_on_off);
        MenuItem authenticateMenuItem = menu.findItem(R.id.action_authenticate);
        MenuItem notificationOn = menu.findItem(R.id.action_notification_on_off);

        enabledDisabledMenuItem.setChecked(ConfigUtil.readBoolean(IS_ENABLED));

        notificationOn.setChecked(ConfigUtil.readBoolean(NOTIFICATION_ON));

        int selectedCount = callArrayAdapter.getCheckedCount();
        String itemTitle;
        if (selectedCount == 0) {
            deleteSelectedMenuItem.setVisible(false);
        } else {
            itemTitle = getString(R.string.menu_item_delete_selected) + " (" + selectedCount + ")";
            deleteSelectedMenuItem.setTitle(itemTitle);
            deleteSelectedMenuItem.setVisible(true);
        }

        String authItemTitle;
        if (((Application) getApplicationContext()).isAuthenticated()) {
            authItemTitle = getString(R.string.menu_item_deauthenticate);
        } else {
            authItemTitle = getString(R.string.menu_item_authenticate);
        }
        authenticateMenuItem.setTitle(authItemTitle);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_authenticate:
                menuItemAuthenticate();
                break;

            case R.id.action_show_settings:
                menuItemShowSettings();
                break;

            case R.id.action_on_off:
                menuItemOnOff();
                break;

            case R.id.action_notification_on_off:
                menuItemNotificationOnOff();
                break;

            case R.id.action_delete_selected:
                menuItemDeleteSelected();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void menuItemAuthenticate() {
        if (((Application) getApplicationContext()).isAuthenticated()) {
            ((Application) getApplicationContext()).setAuthMan(null);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(
                                getResources().getColor(R.color.colorPrimary)));
            }

            Toast.makeText(getApplication(),
                    getString(R.string.toast_deauthenticated),
                    Toast.LENGTH_SHORT).show();

        } else {
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
                                        AuthenticationManager authMan = new AuthenticationManager();
                                        authMan.authenticate(password);
                                        ((Application) getApplicationContext()).setAuthMan(authMan);

                                        ActionBar actionBar = getSupportActionBar();
                                        if (actionBar != null) {
                                            actionBar.setBackgroundDrawable(
                                                    new ColorDrawable(
                                                            getResources().getColor(R.color.colorAuthenticated)));
                                        }

                                        Toast.makeText(getApplication(),
                                                getString(R.string.toast_authenticated),
                                                Toast.LENGTH_SHORT).show();
                                    } catch (AuthenticationException e) {
                                        Toast.makeText(getApplication(),
                                                getString(R.string.toast_authenticated),
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
    }

    private void menuItemShowSettings() {
        Intent settingActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingActivityIntent);
    }

    private void menuItemOnOff() {
        Boolean isEnabledPrevious = ConfigUtil.readBoolean(IS_ENABLED);
        Boolean isEnabledNew = !isEnabledPrevious;
        ConfigUtil.writeValue(IS_ENABLED, isEnabledNew.toString().toLowerCase());
    }

    private void menuItemNotificationOnOff() {
        Boolean isNotificationPrevious = ConfigUtil.readBoolean(NOTIFICATION_ON);
        Boolean isNotificationNew = !isNotificationPrevious;
        ConfigUtil.writeValue(NOTIFICATION_ON, isNotificationNew.toString().toLowerCase());
    }

    private void menuItemDeleteSelected() {
        String toastText;
        if (callArrayAdapter.getCheckedCount() > 0) {
            List<Integer> checkedIndexes = callArrayAdapter.getCheckedStatuses();
            for (Integer i : checkedIndexes) {
                Call call = callArrayAdapter.getItem(i);
                File file = new File(call.getFilename());
                if (file.delete()) {
                    callArrayAdapter.remove(call);
                }
            }
            callArrayAdapter.resetCheckedItems();
            toastText = getString(R.string.toast_records_deleted);
        } else {
            toastText = getString(R.string.toast_nothing_to_delete);
        }
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("CREATED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((Application) getApplicationContext()).setAuthMan(null);
        //todo clear key from memory
        System.out.println("PAUSED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        ((Application) getApplicationContext()).setAuthMan(null);
        //todo clear key from memory
        System.out.println("STOPPED");
    }
}
