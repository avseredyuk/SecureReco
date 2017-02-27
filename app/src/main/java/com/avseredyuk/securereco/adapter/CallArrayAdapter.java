package com.avseredyuk.securereco.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.listener.PlayButtonClickListener;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ContactResolverUtil;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item, parent, false);

        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        firstLine.setText(ContactResolverUtil.getContactName(context,
                calls.get(position).getCallNumber()));
        firstLine.setTextColor(calls.get(position).isIncoming() ? Color.parseColor("#039F00") : Color.BLUE);

        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        secondLine.setText(calls.get(position).getCallNumber());

        TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
        thirdLine.setText(StringUtil.formatDate(calls.get(position).getDatetimeStarted()));

        ImageView imageView = (ImageView) rowView.findViewById(R.id.avatar);
        imageView.setImageResource(R.drawable.ic_launcher);

        ImageButton playBtn = (ImageButton) rowView.findViewById(R.id.playButton);
        playBtn.setTag(calls.get(position));
        playBtn.setOnClickListener(new PlayButtonClickListener(context));

        return rowView;

    }
}
