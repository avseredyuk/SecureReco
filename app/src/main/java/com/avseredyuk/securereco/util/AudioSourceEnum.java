package com.avseredyuk.securereco.util;

import android.media.MediaRecorder;

/**
 * Created by Anton_Serediuk on 6/16/2017.
 */

public enum AudioSourceEnum {
    VOICE_CALL(MediaRecorder.AudioSource.VOICE_CALL),
    VOICE_COMMUNICATION(MediaRecorder.AudioSource.VOICE_COMMUNICATION);

    private int id;

    AudioSourceEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
