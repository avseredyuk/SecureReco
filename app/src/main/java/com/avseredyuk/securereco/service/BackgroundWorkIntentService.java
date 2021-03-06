package com.avseredyuk.securereco.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.callback.FileCallback;
import com.avseredyuk.securereco.dao.FileCallDao;
import com.avseredyuk.securereco.dao.SQLiteCallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.IOUtil;

import java.io.File;
import java.util.List;

import static com.avseredyuk.securereco.util.Constant.BWIS_ACTION;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_CHANGE_FOLDER;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_REGENERATE_KEYS;
import static com.avseredyuk.securereco.util.Constant.NEW_FOLDER_PATH;
import static com.avseredyuk.securereco.util.Constant.OLD_FOLDER_PATH;
import static com.avseredyuk.securereco.util.Constant.OLD_PRIVATE_KEY_INTENT_EXTRA_NAME;

/**
 * Created by Anton_Serediuk on 5/12/2017.
 */

public class BackgroundWorkIntentService extends IntentService {
    public static volatile boolean isRunning;
    public static volatile String action;
    private final Handler handler;

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
        //todo: lock the activities
        final String newFolder = intent.getStringExtra(NEW_FOLDER_PATH);
        new File(newFolder).mkdir();
        String oldFolder = intent.getStringExtra(OLD_FOLDER_PATH);
        FileCallback callback = new FileCallback() {
            @Override
            public void execute(File file) {
                if (IOUtil.copyFile(file, new File(newFolder, file.getName()))) {
                    file.delete();
                }
            }
        };
        IOUtil.processFiles(oldFolder, callback);
        handler.post(new DisplayToast(this, getString(R.string.toast_calls_folder_changed)));
        //todo: unlock the activities
    }

    private void handleRegenerateKeys(Intent intent) {
        //todo: lock the activities
        byte[] oldPrivateKey = intent.getByteArrayExtra(OLD_PRIVATE_KEY_INTENT_EXTRA_NAME);
        SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
        FileCallDao fileDao = FileCallDao.getInstance();
        List<Call> calls = dao.findAll();
        dao.close();
        if (!calls.isEmpty()) {
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_started)));
            for (Call call : calls) {
                fileDao.reEncryptHeader(call, oldPrivateKey);
            }
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_finished)));
        } else {
            handler.post(new DisplayToast(this, getString(R.string.toast_keys_regen_update_nothing)));
        }
        //todo: unlock the activities
    }

    private class DisplayToast implements Runnable {
        private final Context mContext;
        private final String mText;
        DisplayToast(Context mContext, String text){
            this.mContext = mContext;
            mText = text;
        }
        public void run(){
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
