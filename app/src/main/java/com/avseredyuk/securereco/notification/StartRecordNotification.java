package com.avseredyuk.securereco.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.model.NotificationColor;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.avseredyuk.securereco.util.Constant.INTENT_CANCEL_NOTIFICATION;
import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;
import static com.avseredyuk.securereco.util.Constant.INTENT_START_RECORD;

/**
 * Created by Anton_Serediuk on 6/29/2017.
 */

public class StartRecordNotification extends ApplicationNotification {

    {
        button1 = notificationColor == NotificationColor.DAY ? R.drawable.ic_fiber_manual_record_black_24dp : R.drawable.ic_fiber_manual_record_white_24dp;
        button2 = notificationColor == NotificationColor.DAY ? R.drawable.ic_cancel_black_24dp : R.drawable.ic_cancel_white_24dp;
        notificationText1 = context.getString(R.string.notification_start_record_question);
        notificationText2 = String.format(context.getString(R.string.notification_start_record_name_format), call.getContactName());
        button1PendingIntent = PendingIntent.getBroadcast(context, 0, new Intent().setAction(INTENT_START_RECORD).putExtra(INTENT_EXTRA_CALL_DATA, call), FLAG_UPDATE_CURRENT);
        button2PendingIntent = PendingIntent.getBroadcast(context, 1, new Intent().setAction(INTENT_CANCEL_NOTIFICATION), FLAG_UPDATE_CURRENT);
    }

    public StartRecordNotification(Context context, Call call) {
        super(context, call);
    }
}
