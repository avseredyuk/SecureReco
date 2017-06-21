package com.avseredyuk.securereco.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.activity.MainActivity;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.AudioSourceEnum;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.ContactResolverUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Date;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class PhonecallReceiver extends BroadcastReceiver {
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private String savedNumber;
    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private ParcelFileDescriptor[] pipe;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConfigUtil.readBoolean(IS_ENABLED)) {
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                savedNumber = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
            } else if (INTENT_START_RECORD.equals(intent.getAction())) {
                //todo: start record somehow
            } else if (INTENT_CANCEL_START_RECORD_NOTIFICATION.equals(intent.getAction())) {
                cancelNotification(context);
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state, number);
            }
        }
    }

    private void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // onIncomingCallReceived
                savedNumber = number;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // onOutgoingCallStarted, onIncomingCallAnswered

                Call callToRecord = new Call(savedNumber, new Date(), lastState == TelephonyManager.CALL_STATE_RINGING);

                // boolean toRecordOrNotToRecord = SuperSmartDecisionMakerBasedOnFiltersOrStrategies.decide();
                // todo: remove this stub
                boolean toRecordOrNotToRecord = false;

                if (toRecordOrNotToRecord){
                    startRecording(callToRecord);
                    setUpRecordStartedNotification(context, callToRecord);
                } else {
                    setUpStartRecordNotification(context, callToRecord);
                }


                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // onMissedCall
                    break;
                } else {
                    // onIncomingCallEnded, onOutgoingCallEnded
                    stopRecording();
                }
                doNotificationStuff(context);
                break;
        }
        lastState = state;
    }

    private void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    private void setUpRecordStartedNotification(Context context, Call call) {
        //todo: record successfully started, show buttons to manage current record process
        System.out.println(call);
    }

    private void setUpStartRecordNotification(Context context, Call call) {
        if (ConfigUtil.readBoolean(NOTIFICATION_ON)) {
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_start_record);

            contentView.setImageViewBitmap(R.id.notification_contact_photo,
                    ContactResolverUtil.retrieveContactPhotoCircleCropped(context, call.getCallNumber()));

            contentView.setTextViewText(R.id.notification_text_1, context.getString(R.string.notification_start_record_question));
            contentView.setTextViewText(R.id.notification_text_2, String.format(context.getString(R.string.notification_start_record_name_format), call.getCallNumber()));

            Intent intentStartRecord = new Intent().setAction(INTENT_START_RECORD);
            PendingIntent pIntentStartRecord = PendingIntent.getBroadcast(context, 0, intentStartRecord, PendingIntent.FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_record, pIntentStartRecord);

            Intent intentCancelStartRecordNotification = new Intent().setAction(INTENT_CANCEL_START_RECORD_NOTIFICATION);
            PendingIntent pIntentCancelStartRecordNotification = PendingIntent.getBroadcast(context, 1, intentCancelStartRecordNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            contentView.setOnClickPendingIntent(R.id.notification_button_cancel, pIntentCancelStartRecordNotification);

            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.button_play)
                    .setContent(contentView)
                    .setOngoing(true)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }

    private void doNotificationStuff(Context context) {
        if (ConfigUtil.readBoolean(NOTIFICATION_ON)) {
            PendingIntent myPendingIntent = PendingIntent.getActivity(context,
                    0,
                    new Intent(context, MainActivity.class),
                    0);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.button_play)
                    .setContentIntent(myPendingIntent)
                    .setAutoCancel(true)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }

    private void startRecording(Call call) {
        System.out.println(">>>>>>>>>>>>>>>>>>> REC START");
        if (recordStarted) {
            stopRecording();
        }


            recorder = new MediaRecorder();
            recorder.setAudioSource(
                    AudioSourceEnum.valueOf(
                            ConfigUtil.readValue(AUDIO_SOURCE)
                    ).getId()
            );
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                recorder.setOutputFile(getStreamFd(call));
                recorder.prepare();
                recorder.start();
                recordStarted = true;

            } catch (IllegalStateException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at starting recording: audio source not set", e);
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at starting recording", e);
            }


    }

    private FileDescriptor getStreamFd(Call call) throws IOException {
        pipe = ParcelFileDescriptor.createPipe();

        new PipeProcessingThread(
                call,
                new ParcelFileDescriptor.AutoCloseInputStream(pipe[0])
        ).start();

        return pipe[1].getFileDescriptor();
    }

    private void stopRecording() {
        System.out.println(">>>>>>>>>>>>>>>>>>> REC END");
        if (recordStarted) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recordStarted = false;
            try {
                pipe[1].close();
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at closing pipe during recording", e);
            }
        }
    }
}
