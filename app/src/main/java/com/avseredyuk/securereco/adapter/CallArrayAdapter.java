package com.avseredyuk.securereco.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.listener.PlayButtonClickListener;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ContactResolverUtil;
import com.avseredyuk.securereco.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lenfer on 2/15/17.
 */
public class CallArrayAdapter extends ArrayAdapter<Call> {
    Context context;
    List<Call> calls;
    Boolean[] checkedStatuses;

    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
        TextView thirdLine;
        ImageView imageView;
        ImageButton playBtn;
        CheckBox checkBox;
    }

    public CallArrayAdapter(Context context, List<Call> calls) {
        super(context, R.layout.list_item, calls);
        this.context = context;
        this.calls = calls;
        checkedStatuses = new Boolean[calls.size()];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Call call = calls.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            viewHolder.firstLine = (TextView) convertView.findViewById(R.id.contactName);
            viewHolder.secondLine = (TextView) convertView.findViewById(R.id.callNumber);
            viewHolder.thirdLine = (TextView) convertView.findViewById(R.id.callDate);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
            viewHolder.playBtn = (ImageButton) convertView.findViewById(R.id.playButton);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.firstLine.setText(ContactResolverUtil.getContactName(context, call.getCallNumber()));
        viewHolder.firstLine.setTextColor(call.isIncoming() ? Color.parseColor("#039F00") : Color.BLUE);
        viewHolder.secondLine.setText(call.getCallNumber());
        viewHolder.thirdLine.setText(StringUtil.formatDate(call.getDatetimeStarted()));
        viewHolder.imageView.setImageBitmap(ContactResolverUtil.retrieveContactPhoto(context, call.getCallNumber()));
        viewHolder.playBtn.setTag(call);
        viewHolder.playBtn.setOnClickListener(new PlayButtonClickListener(context));

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkedStatuses[position] = isChecked;
            }
        });
        Boolean isItemChecked = checkedStatuses[position];
        if (isItemChecked == null) {
            isItemChecked = false;
        }
        viewHolder.checkBox.setChecked(isItemChecked);

        return convertView;
    }
}
