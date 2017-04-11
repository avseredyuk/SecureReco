package com.avseredyuk.securereco.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.StringUtil;

import java.io.File;
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
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
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
                } else if (isIncoming) {
                    stopRecording();
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    stopRecording();
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
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
                new FileOutputStream(getOutputFile())).start();
        return pipe[1].getFileDescriptor();
    }

    private File getOutputFile() {
        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/" + CALL_LOGS_DIRECTORY);
        sampleDir.mkdirs();
        return new File(sampleDir, StringUtil.formatFileName(savedNumber, callStartTime, isIncoming));
    }

    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.d("onIncomingCallReceived", number + " " + start.toString());
    }

    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.d("onIncomingCallAnswered", number + " " + start.toString());
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("onIncomingCallEnded", number + " " + start.toString() + "\t" + end.toString());
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d("onOutgoingCallStarted", number + " " + start.toString());
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("onOutgoingCallEnded", number + " " + start.toString() + "\t" + end.toString());
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.d("onMissedCall", number + " " + start.toString());
    }
}
