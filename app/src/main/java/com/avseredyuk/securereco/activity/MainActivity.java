package com.avseredyuk.securereco.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.adapter.CallArrayAdapter;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.io.File;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.IS_ENABLED;

public class MainActivity extends AppCompatActivity {
    private MenuItem enabledDisabledMenuItem;
    private MenuItem deleteSelectedMenuItem;
    private CallArrayAdapter callArrayAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        ListView callsListView = (ListView) findViewById(R.id.listView);
        List<Call>  calls = CallDao.getInstance().findAll(Call.CallDateComparator);
        callArrayAdapter = new CallArrayAdapter(this, calls);
        callsListView.setAdapter(callArrayAdapter);
        System.out.println("RESUMED");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        enabledDisabledMenuItem = menu.findItem(R.id.action_on_off);
        deleteSelectedMenuItem = menu.findItem(R.id.action_delete_selected);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        enabledDisabledMenuItem.setChecked(ConfigUtil.readBoolean(IS_ENABLED));
        int selectedCount = callArrayAdapter.getCheckedCount();
        String itemTitle;
        if (selectedCount == 0) {
            deleteSelectedMenuItem.setVisible(false);
        } else {
            itemTitle = getString(R.string.menu_item_delete_selected) + " (" + selectedCount + ")";
            deleteSelectedMenuItem.setTitle(itemTitle);
            deleteSelectedMenuItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //todo: refactor this trash
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_settings:
                Intent newActivity = new Intent(this, SettingsActivity.class);
                startActivity(newActivity);
                return true;

            case R.id.action_on_off:
                Boolean isEnabled = ConfigUtil.readBoolean(IS_ENABLED);
                ConfigUtil.writeValue(IS_ENABLED, isEnabled.toString().toLowerCase());
                return true;

            case R.id.action_delete_selected:
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
                    toastText = "Records deleted";
                } else {
                    toastText = "Nothing to delete";
                }
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
        //todo clear key from memory
        System.out.println("PAUSED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //todo clear key from memory
        System.out.println("STOPPED");
    }
}
