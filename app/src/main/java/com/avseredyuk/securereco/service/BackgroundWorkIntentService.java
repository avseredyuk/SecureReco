package com.avseredyuk.securereco.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;

import java.util.List;

import static com.avseredyuk.securereco.util.Constant.BWIS_ACTION;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_CHANGE_FOLDER;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_REGENERATE_KEYS;
import static com.avseredyuk.securereco.util.Constant.OLD_PRIVATE_KEY_INTENT_EXTRA_NAME;

/**
 * Created by Anton_Serediuk on 5/12/2017.
 */

public class BackgroundWorkIntentService extends IntentService {
    public static volatile boolean isRunning;
    public static volatile String action;
    private Handler handler;

    public BackgroundWorkIntentService() {
        super("BackgroundWorkIntentService");
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isRunning = true;

        if (intent != null) {
            action = intent.getStringExtra(BWIS_ACTION);
            switch (action) {
                case BWIS_DESTINATION_REGENERATE_KEYS:
                    handleRegenerateKeys(intent);
                    break;
                case BWIS_DESTINATION_CHANGE_FOLDER:
                    handlerChangeFolder(intent);
                    break;
            }
        }


        isRunning = false;
    }

    private void handlerChangeFolder(Intent intent) {
        //todo
    }

    private void handleRegenerateKeys(Intent intent) {
        byte[] oldPrivateKey = intent.getByteArrayExtra(OLD_PRIVATE_KEY_INTENT_EXTRA_NAME);
        CallDao callDao = CallDao.getInstance();
        List<Call> calls = callDao.findAll();
        if (!calls.isEmpty()) {
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_started)));
            for (Call call : calls) {
                callDao.reEncryptHeader(call, oldPrivateKey);
            }
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_finished)));
        } else {
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_nothing)));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;
        public DisplayToast(Context mContext, String text){
            this.mContext = mContext;
            mText = text;
        }
        public void run(){
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
