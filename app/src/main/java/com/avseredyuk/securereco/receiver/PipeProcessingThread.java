package com.avseredyuk.securereco.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.activity.MainActivity;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ArrayUtil;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.crypto.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class PipeProcessingThread extends Thread {
    private final InputStream in;
    private final Call call;

    PipeProcessingThread(Call call, InputStream in) {
        this.in = in;
        this.call = call;
    }

    @Override
    public void run() {
        CipherOutputStream outCipher = null;
        try {
            File callFile = CallDao.getInstance().createTemporaryFile(call);
            FileOutputStream out = new FileOutputStream(callFile);

            Cipher rsaCipher = RSA.getPublicKeyCipher();

            AES.KeyCipherTuple keyCipherTuple = AES.initEncryptWithRandom();
            byte[] buf = new byte[BUF_SIZE];
            int len;

            out.write(rsaCipher.doFinal(ArrayUtil.combineArrays(keyCipherTuple.getKey().getEncoded(),
                    keyCipherTuple.getCipher().getIV())));

            outCipher = new CipherOutputStream(out, keyCipherTuple.getCipher());
            while ((len = in.read(buf)) > 0) {
                outCipher.write(buf, 0, len);
            }

        } catch (GeneralSecurityException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at PipeProcessingThread.run() stuff", e);
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception writing from pool to file", e);
        } finally {
            try {
                if (outCipher != null) {
                    outCipher.close();
                }
            } catch (IOException e) {
                //todo
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                //todo
            }

            // move call log file from temporary to permanent file name
            call.setDateTimeEnded(new Date());
            call.setFilename(CallDao.getInstance().moveFromTempToPermanentFile(call));

            setUpRecordFinishedNotification(Application.getInstance().getApplicationContext(), call);

        }
    }

    private void setUpRecordFinishedNotification(Context context, Call call) {
        if (ConfigUtil.readBoolean(NOTIFICATION_ON)) {

            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_record_finished);

            contentView.setImageViewBitmap(R.id.notification_contact_photo, call.getPhoto());

            contentView.setTextViewText(R.id.notification_text_1, context.getString(R.string.notification_record_finished_header));
            contentView.setTextViewText(R.id.notification_text_2, String.format(context.getString(R.string.notification_start_record_name_format), call.getContactName()));

            Intent intentOpenRecorded = new Intent(context, MainActivity.class).putExtra(INTENT_EXTRA_CALL_DATA, call);
            PendingIntent pIntentOpenRecorded = PendingIntent.getActivity(context, 0, intentOpenRecorded, FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_open, pIntentOpenRecorded);

            Intent intentCancelRecordFinishedNotification = new Intent().setAction(INTENT_CANCEL_NOTIFICATION);
            PendingIntent pIntentCancelRecordFinishedNotification = PendingIntent.getBroadcast(context, 1, intentCancelRecordFinishedNotification, FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_cancel, pIntentCancelRecordFinishedNotification);

            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.button_play)
                    .setContent(contentView)
                    .setOngoing(true)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }
}
