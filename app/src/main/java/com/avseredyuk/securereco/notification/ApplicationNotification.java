package com.avseredyuk.securereco.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.model.NotificationColor;

import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ID;

/**
 * Created by Anton_Serediuk on 6/29/2017.
 */

public abstract class ApplicationNotification {
    protected final NotificationColor notificationColor =
            Application.getInstance().getConfiguration().getNotificationColor();
    final Context context;
    protected final Call call;
    private RemoteViews contentView;

    int button1;
    int button2;
    String notificationText1;
    String notificationText2;
    PendingIntent button1PendingIntent;
    PendingIntent button2PendingIntent;


    ApplicationNotification(Context context, Call call) {
        this.context = context;
        this.call = call;
    }

    public void alert() {
        if (Application.getInstance().getConfiguration().isNotificationOn()) {

            initializeLayout();

            contentView.setImageViewBitmap(R.id.notification_contact_photo, call.getPhoto());

            android.app.Notification notification = new android.app.Notification.Builder(context)
                    .setSmallIcon(R.drawable.button_play)
                    .setContent(contentView)
                    .setOngoing(true)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }

    void initializeLayout() {
        // Common init
        contentView = new RemoteViews(context.getPackageName(),
                R.layout.notification_layout);
        contentView.setInt(R.id.notificationLayout, "setBackgroundResource",
                notificationColor == NotificationColor.DAY
                        ? R.color.notificationBgColor
                        : R.color.notificationTextColor);
        contentView.setTextColor(R.id.notification_text_1, context.getResources().getColor(
                notificationColor == NotificationColor.DAY
                        ? R.color.notificationTextColor
                        : R.color.notificationBgColor));
        contentView.setTextColor(R.id.notification_text_2, context.getResources().getColor(
                notificationColor == NotificationColor.DAY
                        ? R.color.notificationTextColor
                        : R.color.notificationBgColor));
        // Individual init
        contentView.setImageViewResource(R.id.notification_button_1, button1);
        contentView.setImageViewResource(R.id.notification_button_2, button2);
        contentView.setTextViewText(R.id.notification_text_1, notificationText1);
        contentView.setTextViewText(R.id.notification_text_2, notificationText2);
        contentView.setOnClickPendingIntent(R.id.notification_button_1, button1PendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_button_2, button2PendingIntent);
    }
}
