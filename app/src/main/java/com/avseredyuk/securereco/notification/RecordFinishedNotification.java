package com.avseredyuk.securereco.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.activity.MainActivity;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.model.Call;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.avseredyuk.securereco.util.Constant.INTENT_CANCEL_NOTIFICATION;
import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ID;

/**
 * Created by Anton_Serediuk on 6/29/2017.
 */

public class RecordFinishedNotification implements ApplicationNotification {
    private final Context context;
    private final Call call;

    public RecordFinishedNotification(Context context, Call call) {
        this.context = context;
        this.call = call;
    }

    @Override
    public void alert() {
        if (Application.getInstance().getConfiguration().isNotificationOn()) {
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
