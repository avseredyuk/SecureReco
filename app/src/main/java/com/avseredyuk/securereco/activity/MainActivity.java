package com.avseredyuk.securereco.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.ContactResolverUtil;
import com.avseredyuk.securereco.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.IS_ENABLED;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ON;

public class MainActivity extends AppCompatActivity
        implements MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {
    private CallArrayAdapter callArrayAdapter;
    private List<Call> calls = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private Handler handler = new Handler();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView callsListView = (ListView) findViewById(R.id.listView);
        callArrayAdapter = new CallArrayAdapter(this, calls);
        callsListView.setAdapter(callArrayAdapter);
        System.out.println("MA CREATED");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Application.getInstance().authHolder.tryLock()) {
            Log.e("LOCK","MainActivity.onResume() on resume can't lock");
        }

        calls.clear();
        calls.addAll(CallDao.getInstance().findAll(Call.CallDateComparator));
        callArrayAdapter.notifyDataSetChanged();

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

        System.out.println("MA RESUMED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getInstance().authHolder.unlock();
        System.out.println("MA PAUSED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Application.getInstance().authHolder.isLocked()) {
            Application.getInstance().eraseAuthMan();
        }
        destroyMedia();

        System.out.println("MA STOPPED");
    }

    private void destroyMedia() {
        if (mediaController != null) {
            mediaController.hide();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
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
        if (Application.getInstance().isAuthenticated()) {
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
        if (Application.getInstance().isAuthenticated()) {
            Application.getInstance().eraseAuthMan();

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
                                        AuthenticationManager
                                                .newAuthManWithAuthentication(password)
                                                .setAsApplicationAuthenticationManager();

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
                                        Log.e(this.getClass().getSimpleName(),
                                                "Error during authentication at MainActivity", e);
                                        Toast.makeText(getApplication(),
                                                getString(R.string.toast_error),
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
            for (int i : checkedIndexes) {
                Call call = callArrayAdapter.getItem(i);
                if (CallDao.getInstance().delete(call)) {
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

    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
        TextView thirdLine;
        ImageView imageView;
        ImageButton playBtn;
        CheckBox checkBox;
    }

    class CallArrayAdapter extends ArrayAdapter<Call>
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        private final List<Integer> checkedItemsIndexes = new ArrayList<>();

        public CallArrayAdapter(Context context, List<Call> calls) {
            super(context, R.layout.list_item, calls);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Call call = this.getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);

                viewHolder.firstLine = (TextView) convertView.findViewById(R.id.contactName);
                viewHolder.secondLine = (TextView) convertView.findViewById(R.id.callNumber);
                viewHolder.thirdLine = (TextView) convertView.findViewById(R.id.callDate);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.playBtn = (ImageButton) convertView.findViewById(R.id.playButton);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.firstLine.setText(ContactResolverUtil.getContactName(getContext(), call.getCallNumber()));
            viewHolder.firstLine.setTextColor(
                    call.isIncoming()
                            ? getContext().getResources().getColor(R.color.colorCallIncoming)
                            : getContext().getResources().getColor(R.color.colorCallOutgoing));
            viewHolder.secondLine.setText(call.getCallNumber());
            viewHolder.thirdLine.setText(StringUtil.formatDate(call.getDatetimeStarted()));
            viewHolder.imageView.setImageBitmap(ContactResolverUtil.retrieveContactPhoto(getContext(), call.getCallNumber()));
            viewHolder.playBtn.setTag(call);
            viewHolder.playBtn.setOnClickListener(this);
            viewHolder.checkBox.setTag(position);
            viewHolder.checkBox.setOnCheckedChangeListener(this);
            boolean isItemChecked = checkedItemsIndexes.contains(position);
            viewHolder.checkBox.setChecked(isItemChecked);

            return convertView;
        }

        public List<Integer> getCheckedStatuses() {
            return new ArrayList<>(checkedItemsIndexes);
        }

        public int getCheckedCount() {
            return checkedItemsIndexes.size();
        }

        public void resetCheckedItems() {
            checkedItemsIndexes.clear();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Integer position = (Integer) buttonView.getTag();
            if (isChecked) {
                checkedItemsIndexes.add(position);
            } else {
                checkedItemsIndexes.remove(position);
            }
        }

        @Override
        public void onClick(View v) {
            Call call = (Call) v.getTag();

            if (Application.getInstance().isAuthenticated()) {
                destroyMedia();

                byte[] callData = CallDao.getInstance().getDecryptedCall(call, Application.getInstance().getAuthMan());

                String base64EncodedString = Base64.encodeToString(callData, Base64.DEFAULT);
                mediaController = new MediaController(MainActivity.this) {
                    @Override
                    public void show(int timeout) {
                        super.show(0);
                    }
                };
                try {
                    String url = "data:audio/amr;base64,"+base64EncodedString;
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.setOnPreparedListener(MainActivity.this);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch(Exception e){
                    System.out.print(e.getMessage());
                }
            } else {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.toast_please_authenticate_first),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        System.out.println("__________________________________________ PREPARED");
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.main_activity));

        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
