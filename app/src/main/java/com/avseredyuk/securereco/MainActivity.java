package com.avseredyuk.securereco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.service.RecorderService;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ArrayList<Call> calls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConfigUtil.isKeysPresent()) {
            Intent intent = new Intent(this, PrepareActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        ListView callsList = (ListView) findViewById(R.id.listView);
        calls = new ArrayList<>();
        /*
        calls.add(new Call("1234", new Date(), new Date(), false));
        calls.add(new Call("4567", new Date(), new Date(), true));
        calls.add(new Call("1234", new Date(), new Date(), false));
        calls.add(new Call("4567", new Date(), new Date(), true));
        calls.add(new Call("1234", new Date(), new Date(), false));
        calls.add(new Call("4567", new Date(), new Date(), true));
        calls.add(new Call("1234", new Date(), new Date(), false));
        calls.add(new Call("4567", new Date(), new Date(), true));
        calls.add(new Call("1234", new Date(), new Date(), false));
        calls.add(new Call("4567", new Date(), new Date(), true));
        */


        callsList.setAdapter(new CallArrayAdapter(this, calls));

        startService(new Intent(MainActivity.this, RecorderService.class));
    }

    /*
    private class CallAdapter extends ArrayAdapter<Call> {
        Call[] values;
        Context context;

        public CallAdapter(Context context, int resource, Call[] objects) {
            super(context, resource, objects);
            this.context = context;
            this.values = objects;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate()

            //return super.getView(position, convertView, parent);
        }

    }*/

}
