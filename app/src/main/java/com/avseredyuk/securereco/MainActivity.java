package com.avseredyuk.securereco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.avseredyuk.securereco.adapter.CallArrayAdapter;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.service.RecorderService;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        ListView callsListView = (ListView) findViewById(R.id.listView);
        List<Call> calls = new CallDao().findAllSortedByDate();
        callsListView.setAdapter(new CallArrayAdapter(this, calls));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConfigUtil.isKeysPresent()) {
            Intent intent = new Intent(this, PrepareActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        ListView callsListView = (ListView) findViewById(R.id.listView);
        List<Call> calls = new CallDao().findAllSortedByDate();
        callsListView.setAdapter(new CallArrayAdapter(this, calls));

        startService(new Intent(MainActivity.this, RecorderService.class));
    }

}
