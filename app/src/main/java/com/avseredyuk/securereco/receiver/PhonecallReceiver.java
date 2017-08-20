package com.avseredyuk.securereco.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.notification.RecordStartedNotification;
import com.avseredyuk.securereco.notification.StartRecordNotification;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Date;

import static com.avseredyuk.securereco.util.Constant.INTENT_CANCEL_NOTIFICATION;
import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;
import static com.avseredyuk.securereco.util.Constant.INTENT_START_RECORD;
import static com.avseredyuk.securereco.util.Constant.INTENT_STOP_RECORD;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ID;

/**
 * Created by lenfer on 2/15/17.
 */
public class PhonecallReceiver extends BroadcastReceiver {
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private Call savedCall;
    private String savedNumber;
    private MediaRecorder recorder;
    private boolean recordStarted = false;
    private ParcelFileDescriptor[] pipe;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Application.getInstance().getConfiguration().isEnabled()) {
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                savedNumber = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
            } else if (INTENT_START_RECORD.equals(intent.getAction())) {
                savedCall = intent.getParcelableExtra(INTENT_EXTRA_CALL_DATA);
                startRecording(savedCall);
                new RecordStartedNotification(
                        context,
                        savedCall
                ).alert();
            } else if (INTENT_CANCEL_NOTIFICATION.equals(intent.getAction())) {
                cancelNotification(context);
            } else if (INTENT_STOP_RECORD.equals(intent.getAction())) {
                stopRecording();
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = TelephonyManager.CALL_STATE_IDLE;
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

                savedCall = new Call(savedNumber, new Date(), lastState == TelephonyManager.CALL_STATE_RINGING);

                // boolean toRecordOrNotToRecord = SuperSmartDecisionMakerBasedOnFiltersOrStrategies.decide();
                // todo: remove this stub
                boolean toRecordOrNotToRecord = true;

                if (toRecordOrNotToRecord){
                    startRecording(savedCall);
                    new RecordStartedNotification(
                            context,
                            savedCall
                    ).alert();
                } else {
                    new StartRecordNotification(
                            context,
                            savedCall
                    ).alert();
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
                break;
        }
        lastState = state;
    }

    private void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    private void startRecording(Call call) {
        System.out.println(">>>>>>>>>>>>>>>>>>> REC START");
        if (recordStarted) {
            stopRecording();
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(Application.getInstance().getConfiguration().getAudioSource().getId());
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
