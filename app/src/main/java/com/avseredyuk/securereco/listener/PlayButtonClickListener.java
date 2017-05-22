package com.avseredyuk.securereco.listener;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

/**
 * Created by lenfer on 2/26/17.
 */
public class PlayButtonClickListener implements View.OnClickListener {
    private final Context context;

    public PlayButtonClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        final Call call = (Call) v.getTag();

        if (Application.getInstance().isAuthenticated()) {
            CallDao.getInstance().play(call,
                    Application.getInstance().getAuthMan());
        } else {
            Toast.makeText(context,
                    context.getString(R.string.toast_please_authenticate_first),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
