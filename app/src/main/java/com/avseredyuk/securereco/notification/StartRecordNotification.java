package com.avseredyuk.securereco.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.avseredyuk.securereco.util.Constant.INTENT_CANCEL_NOTIFICATION;
import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;
import static com.avseredyuk.securereco.util.Constant.INTENT_START_RECORD;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ID;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ON;

/**
 * Created by Anton_Serediuk on 6/29/2017.
 */

public class StartRecordNotification implements ApplicationNotification {
    private Context context;
    private Call call;

    public StartRecordNotification(Context context, Call call) {
        this.context = context;
        this.call = call;
    }

    @Override
    public void alert() {
        if (ConfigUtil.readBoolean(NOTIFICATION_ON)) {
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_start_record);

            contentView.setImageViewBitmap(R.id.notification_contact_photo, call.getPhoto());

            contentView.setTextViewText(R.id.notification_text_1, context.getString(R.string.notification_start_record_question));
            contentView.setTextViewText(R.id.notification_text_2, String.format(context.getString(R.string.notification_start_record_name_format), call.getContactName()));

            Intent intentStartRecord = new Intent().setAction(INTENT_START_RECORD);
            intentStartRecord.putExtra(INTENT_EXTRA_CALL_DATA, call);
            PendingIntent pIntentStartRecord = PendingIntent.getBroadcast(context, 0, intentStartRecord, FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_record, pIntentStartRecord);

            Intent intentCancelStartRecordNotification = new Intent().setAction(INTENT_CANCEL_NOTIFICATION);
            PendingIntent pIntentCancelStartRecordNotification = PendingIntent.getBroadcast(context, 1, intentCancelStartRecordNotification, FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_cancel, pIntentCancelStartRecordNotification);

            android.app.Notification notification = new android.app.Notification.Builder(context)
                    .setSmallIcon(R.drawable.button_play)
                    .setContent(contentView)
                    .setOngoing(true)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }
}
