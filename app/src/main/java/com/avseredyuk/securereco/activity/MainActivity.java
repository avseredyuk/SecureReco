package com.avseredyuk.securereco.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.adapter.CallArrayAdapter;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.util.List;

import static com.avseredyuk.securereco.util.Constant.*;

public class MainActivity extends AppCompatActivity {
    private MenuItem enabledDisabledMenuItem;

    @Override
    protected void onResume() {
        super.onResume();
        ListView callsListView = (ListView) findViewById(R.id.listView);
        List<Call> calls = CallDao.getInstance().findAll(Call.CallDateComparator);
        callsListView.setAdapter(new CallArrayAdapter(this, calls));
        System.out.println("RESUMED");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        enabledDisabledMenuItem = menu.findItem(R.id.action_on_off);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        enabledDisabledMenuItem.setChecked(ConfigUtil.readBoolean(IS_ENABLED));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_settings:
                Intent newActivity = new Intent(this, SettingsActivity.class);
                startActivity(newActivity);
                return true;
            case R.id.action_on_off:
                if (ConfigUtil.readBoolean(IS_ENABLED)) {
                    ConfigUtil.writeValue(IS_ENABLED, "false");
                } else {
                    ConfigUtil.writeValue(IS_ENABLED, "true");
                }
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
