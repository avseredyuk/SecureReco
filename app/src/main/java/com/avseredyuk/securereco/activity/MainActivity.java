package com.avseredyuk.securereco.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.callback.Callback;
import com.avseredyuk.securereco.callback.PlayCallCallback;
import com.avseredyuk.securereco.dao.FileCallDao;
import com.avseredyuk.securereco.dao.SQLiteCallDao;
import com.avseredyuk.securereco.filedialog.FileOperation;
import com.avseredyuk.securereco.filedialog.FileSelector;
import com.avseredyuk.securereco.filedialog.OnHandleFileListener;
import com.avseredyuk.securereco.media.PermanentMediaController;
import com.avseredyuk.securereco.media.MediaDestroyer;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.CALLS_LIST_PARCEL_NAME;
import static com.avseredyuk.securereco.util.Constant.INTENT_CANCEL_NOTIFICATION;
import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;

public class MainActivity extends SecuredActivity
        implements MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl,
        MediaDestroyer,
        SearchView.OnQueryTextListener {
    private static final String AMR_SUFFIX = ".amr";
    private static final String[] mFileFilter = { "*.*", ".amr" };
    private final Handler handler = new Handler();
    private final List<Call> calls = new ArrayList<>();
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaController mediaController;
    private CallArrayAdapter callArrayAdapter;
    private List<Call> originalCalls = new ArrayList<>();
    private Menu menu;
    private String filterString;

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
        callsListView.setEmptyView(findViewById(R.id.emptyElement));
        callsListView.setTextFilterEnabled(true);
        mediaController = new PermanentMediaController(MainActivity.this, MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        List<Call> callsListFromIntent = intent.getParcelableArrayListExtra(CALLS_LIST_PARCEL_NAME);
        originalCalls.clear();
        if (callsListFromIntent != null) {
            originalCalls.addAll(callsListFromIntent);
            intent.putExtra(CALLS_LIST_PARCEL_NAME, (String) null);
        } else {
            SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
            originalCalls.addAll(dao.findAllOrderedByDate());
            dao.close();
        }
        calls.clear();
        calls.addAll(originalCalls);
        callArrayAdapter.notifyDataSetChanged();

        if ((callArrayAdapter.filter != null) && (filterString != null)) {
            callArrayAdapter.filter.filter(filterString);
        }

        Call callToOpen = intent.getParcelableExtra(INTENT_EXTRA_CALL_DATA);
        if (callToOpen != null) {
            intent.putExtra(INTENT_EXTRA_CALL_DATA, (String) null);
            sendBroadcast(new Intent().setAction(INTENT_CANCEL_NOTIFICATION));
            Callback playCallCallback = new PlayCallCallback(MainActivity.this, callToOpen, MainActivity.this, mediaPlayer);
            if (Application.getInstance().isAuthenticated()) {
                playCallCallback.execute(null);
            } else {
                makeAlertDialog(playCallCallback);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void updateUIOnAuthenticationReset() {
        super.updateUIOnAuthenticationReset();
        setAuthMenuItemText(menu.findItem(R.id.action_authenticate));
        destroyMedia(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyMedia(true);
    }

    @Override
    public void destroyMedia(boolean hideUi) {
        if (hideUi) {
            mediaController.hide();
        }
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            Log.d(this.getClass().getSimpleName(), "Error destroying media", e);
        }
        mediaPlayer.reset();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        callArrayAdapter.getFilter().filter(query);
        filterString = query;
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        callArrayAdapter.getFilter().filter(query);
        filterString = query;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(getString(R.string.action_bar_query_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteSelectedMenuItem = menu.findItem(R.id.action_delete_selected);
        MenuItem enabledDisabledMenuItem = menu.findItem(R.id.action_on_off);
        MenuItem authenticateMenuItem = menu.findItem(R.id.action_authenticate);
        MenuItem notificationOn = menu.findItem(R.id.action_notification_on_off);

        enabledDisabledMenuItem.setChecked(Application.getInstance().getConfiguration().isEnabled());

        notificationOn.setChecked(Application.getInstance().getConfiguration().isNotificationOn());

        int selectedCount = callArrayAdapter.getCheckedCalls().size();
        String itemTitle;
        if (selectedCount == 0) {
            deleteSelectedMenuItem.setVisible(false);
        } else {
            itemTitle = getString(R.string.menu_item_delete_selected) + " (" + selectedCount + ")";
            deleteSelectedMenuItem.setTitle(itemTitle);
            deleteSelectedMenuItem.setVisible(true);
        }

        setAuthMenuItemText(authenticateMenuItem);

        return super.onPrepareOptionsMenu(menu);
    }

    private void setAuthMenuItemText(MenuItem authenticateMenuItem) {
        String authItemTitle;
        if (Application.getInstance().isAuthenticated()) {
            authItemTitle = getString(R.string.menu_item_deauthenticate);
        } else {
            authItemTitle = getString(R.string.menu_item_authenticate);
        }
        authenticateMenuItem.setTitle(authItemTitle);
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

    private void menuItemShowSettings() {
        Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivityIntent);
    }

    private void menuItemOnOff() {
        Application.getInstance().getConfiguration()
                .setEnabled(!Application.getInstance().getConfiguration().isEnabled());
    }

    private void menuItemNotificationOnOff() {
        Application.getInstance().getConfiguration()
                .setNotificationOn(!Application.getInstance().getConfiguration().isNotificationOn());
    }

    private void menuItemDeleteSelected() {
        String toastText;
        List<Call> checkedCalls = callArrayAdapter.getCheckedCalls();
        if (!checkedCalls.isEmpty()) {
            SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
            for (Call c : checkedCalls) {
                if ((dao.delete(c)) && (FileCallDao.getInstance().delete(c))) {
                    originalCalls.remove(c);
                    callArrayAdapter.remove(c);
                }
            }
            dao.close();
            callArrayAdapter.resetCheckedItems();
            toastText = getString(R.string.toast_records_deleted);
        } else {
            toastText = getString(R.string.toast_nothing_to_delete);
        }
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    private void menuItemAuthenticate() {
        if (Application.getInstance().isAuthenticated()) {
            Application.getInstance().eraseAuthMan();

            updateActionBarColors();
            Toast.makeText(getApplication(),
                    getString(R.string.toast_deauthenticated),
                    Toast.LENGTH_SHORT).show();

        } else {
            makeAlertDialog(null);
        }
    }

    private static class ViewHolder {
        TextView separator;
        TextView firstLine;
        TextView secondLine;
        TextView thirdLine;
        ImageButton starBtn;
        ImageButton playBtn;
        ImageButton moreButton;
        CheckBox checkBox;
        ImageView callType;
    }

    private class CallArrayAdapter extends ArrayAdapter<Call>
            implements View.OnClickListener,
            CompoundButton.OnCheckedChangeListener,
            View.OnLongClickListener,
            Filterable {
        private Filter filter;

        CallArrayAdapter(Context context, List<Call> calls) {
            super(context, R.layout.list_item_v2, calls);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Call call = this.getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_v2, parent, false);

                viewHolder.separator = (TextView) convertView.findViewById(R.id.separator);
                viewHolder.firstLine = (TextView) convertView.findViewById(R.id.contactName);
                viewHolder.secondLine = (TextView) convertView.findViewById(R.id.callTime);
                viewHolder.thirdLine = (TextView) convertView.findViewById(R.id.callDuration);
                viewHolder.starBtn = (ImageButton) convertView.findViewById(R.id.starButton);
                viewHolder.playBtn = (ImageButton) convertView.findViewById(R.id.playButton);
                viewHolder.moreButton = (ImageButton) convertView.findViewById(R.id.moreButton);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                viewHolder.callType = (ImageView) convertView.findViewById(R.id.callType);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if ((position == 0) || (!StringUtil.isSameDay(call.getDateTimeStarted(), this.getItem(position - 1).getDateTimeStarted()))) {
                viewHolder.separator.setText(StringUtil.formatDateOnly(call.getDateTimeStarted()));
                viewHolder.separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.separator.setVisibility(View.GONE);
            }

            viewHolder.firstLine.setText(call.getContactName());
            viewHolder.callType.setImageResource(
                    call.isIncoming()
                            ? R.drawable.ic_call_received_black_24dp
                            : R.drawable.ic_call_made_black_24dp
            );
            viewHolder.secondLine.setText(StringUtil.formatDate(call.getDateTimeStarted()));
            viewHolder.thirdLine.setText(StringUtil.formatTimeInterval(call.getDateTimeStarted(), call.getDateTimeEnded()));
            viewHolder.checkBox.setButtonDrawable(
                    new BitmapDrawable(getResources(), call.getPhoto())
            );
            viewHolder.starBtn.setImageResource(
                    call.isStarred()
                            ? R.drawable.ic_star_24dp
                            : R.drawable.ic_star_border_24dp
            );
            viewHolder.starBtn.setTag(call);
            viewHolder.starBtn.setOnClickListener(new StarButtonOnClickListener());
            viewHolder.playBtn.setTag(call);
            viewHolder.playBtn.setOnClickListener(this);
            viewHolder.playBtn.setOnLongClickListener(this);
            viewHolder.moreButton.setTag(call);
            viewHolder.moreButton.setOnClickListener(new MoreButtonOnClickListener());
            viewHolder.checkBox.setTag(call);
            viewHolder.checkBox.setOnCheckedChangeListener(this);
            viewHolder.checkBox.setChecked(call.isChecked());

            return convertView;
        }

        List<Call> getCheckedCalls() {
            List<Call> resultCalls = new ArrayList<>();
            for (Call c : calls) {
                if (c.isChecked()) {
                    resultCalls.add(c);
                }
            }
            return resultCalls;
        }

        void resetCheckedItems() {
            for (Call c : calls) {
                c.setChecked(false);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Call checkBoxCall = (Call) buttonView.getTag();
            checkBoxCall.setChecked(isChecked);

            View listItemView = (View) buttonView.getParent().getParent();
            View dateHeaderTextView = listItemView.findViewById(R.id.separator);
            if (isChecked) {
                buttonView.setButtonDrawable(R.drawable.ic_check_black_24dp);
                listItemView.setBackgroundColor(getResources().getColor(R.color.listViewBgColorSelected));
            } else {
                View parentRow = (View) buttonView.getParent();
                parentRow.setBackgroundColor(getResources().getColor(R.color.listViewBgColor));
                buttonView.setButtonDrawable(
                        new BitmapDrawable(
                                getResources(),
                                checkBoxCall.getPhoto()
                        )
                );
            }
            if (dateHeaderTextView.getVisibility() != View.GONE) {
                dateHeaderTextView.setBackgroundResource(R.color.listViewHeaderBgColor);
            }

        }

        class MoreButtonOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                final Call call = (Call) v.getTag();
                Intent moreActivityIntent = new Intent(getContext(), MoreActivity.class);
                moreActivityIntent.putExtra(INTENT_EXTRA_CALL_DATA, call);
                startActivity(moreActivityIntent);
            }
        }

        class StarButtonOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                final Call call = (Call) v.getTag();
                call.setStarred(!call.isStarred());
                SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
                dao.updateStarredCall(call);
                dao.close();

                ((ImageButton) v).setImageResource(
                        call.isStarred()
                                ? R.drawable.ic_star_24dp
                                : R.drawable.ic_star_border_24dp
                );
            }
        }

        @Override
        public void onClick(View v) {
            final Call call = (Call) v.getTag();

            Callback playCallCallback = new PlayCallCallback(MainActivity.this, call, MainActivity.this, mediaPlayer);

            if (Application.getInstance().isAuthenticated()) {
                playCallCallback.execute(null);
            } else {
                makeAlertDialog(playCallCallback);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final Call call = (Call) v.getTag();

            Callback exportCallCallback = new ExportCallCallback(call);

            if (Application.getInstance().isAuthenticated()) {
                exportCallCallback.execute(null);
            } else {
                makeAlertDialog(exportCallCallback);
            }

            return true;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            filter = (filter == null) ? new CallFilter() : filter;
            return filter;
        }

        private class CallFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Call> results = new ArrayList<>();
                if (originalCalls == null)
                    originalCalls = new ArrayList<>(calls);
                if (constraint != null) {
                    if (!originalCalls.isEmpty()) {
                        for (final Call c : originalCalls) {
                            if (c.getCallNumber().toLowerCase().contains(constraint.toString())
                                    || c.getContactName().toLowerCase().contains(constraint.toString()))
                                results.add(c);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<Call> resultsList = (ArrayList<Call>) results.values;
                clear();
                for (Call c : resultsList){
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.main_activity));
        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.start();
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

    private class ExportCallCallback implements Callback {
        private final Call call;

        public ExportCallCallback(Call call) {
            this.call = call;
        }

        @Override
        public void execute(String password) {
            new FileSelector(MainActivity.this,
                    FileOperation.SAVE,
                    new OnHandleFileListener() {
                        @Override
                        public void handleFile(String filePath) {
                            filePath = StringUtil.addOrChangeFileExtension(filePath, AMR_SUFFIX);
                            if (FileCallDao.getInstance().exportDecryptedCall(filePath, call, Application.getInstance().getAuthMan())) {
                                Toast.makeText(getApplication(),
                                        getString(R.string.call_exported_successfully),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(),
                                        getString(R.string.call_exported_with_error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    mFileFilter,
                    StringUtil.addOrChangeFileExtension(
                            new File(call.getFilename()).getName(),
                            AMR_SUFFIX
                    )
            ).show();
        }
    }

}
