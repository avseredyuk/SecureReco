package com.avseredyuk.securereco.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.StringUtil;

import java.util.List;

/**
 * Created by lenfer on 2/15/17.
 */
public class CallArrayAdapter extends ArrayAdapter<Call> {
    Context context;
    List<Call> calls;

    public CallArrayAdapter(Context context, List<Call> calls) {
        super(context, R.layout.item, calls);
        this.context = context;
        this.calls = calls;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item, parent, false);

        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        firstLine.setText(calls.get(position).getCallNumber());

        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        secondLine.setText(StringUtil.formatDate(calls.get(position).getDatetimeStarted()));

        ImageView imageView = (ImageView) rowView.findViewById(R.id.avatar);
        imageView.setImageResource(R.drawable.ic_launcher);

        return rowView;

    }
}
