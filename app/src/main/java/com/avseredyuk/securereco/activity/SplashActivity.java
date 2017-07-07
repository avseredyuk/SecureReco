package com.avseredyuk.securereco.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.dao.SQLiteCallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.service.RecorderService;

import java.util.ArrayList;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.CALLS_LIST_PARCEL_NAME;

/**
 * Created by lenfer on 3/1/17.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(Application.getInstance(), RecorderService.class));

        if (Application.getInstance().getConfiguration().isConfigValid()) {
            LoadCallsTask loadTask = new LoadCallsTask();
            loadTask.execute();
        } else {
            startActivity(new Intent(Application.getInstance(), FirstRunActivity.class));
            finish();
        }
    }

    private class LoadCallsTask extends AsyncTask<Void, Void, List<Call>> {
        @Override
        protected List<Call> doInBackground(Void... voids) {
            SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
            List<Call> result = dao.findAllOrderedByDate();
            dao.close();
            return result;
        }

        @Override
        protected void onPostExecute(List<Call> calls) {
            super.onPostExecute(calls);
            startActivity(new Intent(Application.getInstance(), MainActivity.class)
                    .putParcelableArrayListExtra(CALLS_LIST_PARCEL_NAME, (ArrayList<Call>) calls));
            finish();
        }
    }
}
