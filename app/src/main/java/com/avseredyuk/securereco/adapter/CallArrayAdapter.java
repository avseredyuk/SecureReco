package com.avseredyuk.securereco.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ContactResolverUtil;
import com.avseredyuk.securereco.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenfer on 2/15/17.
 */
public class CallArrayAdapter extends ArrayAdapter<Call>
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final List<Integer> checkedItemsIndexes = new ArrayList<>();

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
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Call call = this.getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        viewHolder.firstLine.setText(ContactResolverUtil.getContactName(getContext(), call.getCallNumber()));
        viewHolder.firstLine.setTextColor(
                call.isIncoming()
                        ? getContext().getResources().getColor(R.color.colorCallIncoming)
                        : getContext().getResources().getColor(R.color.colorCallOutgoing));
        viewHolder.secondLine.setText(call.getCallNumber());
        viewHolder.thirdLine.setText(StringUtil.formatDate(call.getDatetimeStarted()));
        viewHolder.imageView.setImageBitmap(ContactResolverUtil.retrieveContactPhoto(getContext(), call.getCallNumber()));
        viewHolder.playBtn.setTag(call);
        viewHolder.playBtn.setOnClickListener(this);
        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setOnCheckedChangeListener(this);
        boolean isItemChecked = checkedItemsIndexes.contains(position);
        viewHolder.checkBox.setChecked(isItemChecked);

        return convertView;
    }

    public List<Integer> getCheckedStatuses() {
        return new ArrayList<>(checkedItemsIndexes);
    }

    public int getCheckedCount() {
        return checkedItemsIndexes.size();
    }

    public void resetCheckedItems() {
        checkedItemsIndexes.clear();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Integer position = (Integer) buttonView.getTag();
        if (isChecked) {
            checkedItemsIndexes.add(position);
        } else {
            checkedItemsIndexes.remove(position);
        }
    }

    @Override
    public void onClick(View v) {
        Call call = (Call) v.getTag();

        if (Application.getInstance().isAuthenticated()) {
            CallDao.getInstance().play(call,
                    Application.getInstance().getAuthMan());
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.toast_please_authenticate_first),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
