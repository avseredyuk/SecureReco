package com.avseredyuk.securereco.activity.listener;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by lenfer on 8/19/17.
 */

public abstract class CustomizedSpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    protected int callsCount = 0;

    public abstract void onItemSelectedCustomHandler(AdapterView<?> adapterView, int position);

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (callsCount > 0) {
            onItemSelectedCustomHandler(adapterView, position);
        } else {
            callsCount++;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }
}
