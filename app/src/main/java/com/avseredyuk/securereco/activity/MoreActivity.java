package com.avseredyuk.securereco.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.widget.ImageView;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.model.Call;

import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;

/**
 * Created by lenfer on 7/9/17.
 */

public class MoreActivity extends SecuredActivity {
    private ImageView photoImageView;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more);

        photoImageView = (ImageView) findViewById(R.id.photoImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Call callToShow = intent.getParcelableExtra(INTENT_EXTRA_CALL_DATA);

        photoImageView.setImageBitmap(callToShow.getPhoto());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
