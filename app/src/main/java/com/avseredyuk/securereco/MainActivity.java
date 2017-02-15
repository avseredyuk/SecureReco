package com.avseredyuk.securereco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.avseredyuk.securereco.service.RecorderService;
import com.avseredyuk.securereco.util.ConfigUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConfigUtil.isKeysPresent()) {
            Intent intent = new Intent(this, PrepareActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);

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
