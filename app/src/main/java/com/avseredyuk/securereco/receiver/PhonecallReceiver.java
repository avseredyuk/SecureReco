package com.avseredyuk.securereco.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.avseredyuk.securereco.util.StringUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class PhonecallReceiver extends BroadcastReceiver {
    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private Date callStartTime;
    private boolean isIncoming;
    private String savedNumber;
    private MediaRecorder recorder;
    private boolean recordstarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
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

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
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
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
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
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
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
        //recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.setOutputFile(getStreamFd());
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
        recordstarted = true;
    }

    private void stopRecording() {
        if (recordstarted) {
            recorder.stop();
            recordstarted = false;
        }
    }

    private FileDescriptor getStreamFd() {
        ParcelFileDescriptor[] pipe=null;

        try {
            pipe=ParcelFileDescriptor.createPipe();

            new PipeProcessingThread(new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]),
                    new FileOutputStream(getOutputFile())).start();
        }
        catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Exception opening pipe", e);
        }

        return pipe[1].getFileDescriptor();
    }

    private File getOutputFile() {
        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecordingDasa1");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        return new File(sampleDir, StringUtil.formatFileName(savedNumber, callStartTime));
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
//        PostCallHandler postCallHandler = new PostCallHandler(number, "janskd" , "")
    }
}
