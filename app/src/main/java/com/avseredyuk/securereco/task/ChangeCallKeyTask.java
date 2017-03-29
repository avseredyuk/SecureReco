package com.avseredyuk.securereco.task;

import android.os.AsyncTask;

import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

import java.util.List;

/**
 * Created by lenfer on 3/7/17.
 */
public class ChangeCallKeyTask extends AsyncTask<Void, Integer, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        List<Call> calls = CallDao.getInstance().findAll();
        for(Call call : calls) {
            System.out.println(call.getFilename());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("PRE");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        System.out.println("POST");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        System.out.println("UPDATE");
    }
}
