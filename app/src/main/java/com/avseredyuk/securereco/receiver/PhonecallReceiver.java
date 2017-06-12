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
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;

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
                startRecording(new Call(savedNumber,
                        new Date(),
                        lastState == TelephonyManager.CALL_STATE_RINGING));
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

    private void startRecording(Call call) {
        System.out.println(">>>>>>>>>>>>>>>>>>> REC START");
        if (recordStarted) {
            stopRecording();
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
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
