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

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.activity.MainActivity;
import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class PhonecallReceiver extends BroadcastReceiver {
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private Date callStartTime;
    private boolean isIncoming;
    private String savedNumber;
    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private ParcelFileDescriptor[] pipe;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConfigUtil.readBoolean(IS_ENABLED)) {
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                savedNumber = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
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
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    startRecording();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    startRecording();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, savedNumber, callStartTime);
                    break;
                } else if (isIncoming) {
                    stopRecording();
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    stopRecording();
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }

                doNotificationStuff(context);

                break;
        }
        lastState = state;
    }

    private void doNotificationStuff(Context context) {
        if (ConfigUtil.readBoolean(NOTIFICATION_ON)) {
            PendingIntent myPendingIntent =
                    PendingIntent.getActivity(context,
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

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_NEW_RECORD_ID, notification);
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.setOutputFile(getStreamFd());
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

    private void stopRecording() {
        if (recordStarted) {
            recorder.stop();
            recordStarted = false;
            try {
                pipe[1].close();
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at closing pipe during recording", e);
            }
        }
    }

    private FileDescriptor getStreamFd() throws IOException{
        pipe = ParcelFileDescriptor.createPipe();
        new PipeProcessingThread(new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]),
                new FileOutputStream(
                        CallDao.getInstance().createFile(
                                new Call(savedNumber, callStartTime, isIncoming)
                        )
                )).start();
        return pipe[1].getFileDescriptor();
    }

    private void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.d("onIncomingCallReceived", number + " " + start.toString());
    }

    private void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.d("onIncomingCallAnswered", number + " " + start.toString());
    }

    private void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("onIncomingCallEnded", number + " " + start.toString() + "\t" + end.toString());
    }

    private void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d("onOutgoingCallStarted", number + " " + start.toString());
    }

    private void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("onOutgoingCallEnded", number + " " + start.toString() + "\t" + end.toString());
    }

    private void onMissedCall(Context ctx, String number, Date start) {
        Log.d("onMissedCall", number + " " + start.toString());
    }
}
