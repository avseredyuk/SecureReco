package com.avseredyuk.securereco.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

import java.util.List;

/**
 * Created by lenfer on 3/7/17.
 */
@Deprecated
public class ChangeCallKeyTask extends AsyncTask<Void, Integer, Void> {
    private final Context context;

    public ChangeCallKeyTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<Call> calls = CallDao.getInstance().findAll();
        for (Call call : calls) {
            System.out.println(call.getFilename());
            try {
                Thread.sleep(100);
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

        Toast.makeText(context, "Keys regeneration finished", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        System.out.println("UPDATE");
    }
}
