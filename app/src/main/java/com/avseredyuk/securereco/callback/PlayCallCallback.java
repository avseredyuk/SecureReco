package com.avseredyuk.securereco.callback;

import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.dao.FileCallDao;
import com.avseredyuk.securereco.media.MediaDestroyer;
import com.avseredyuk.securereco.model.Call;

/**
 * Created by lenfer on 2/24/18.
 */

public class PlayCallCallback implements Callback {
    private final MediaPlayer.OnPreparedListener listener;
    private final Call call;
    private final MediaDestroyer mediaDestroyer;
    private MediaPlayer mediaPlayer;

    public PlayCallCallback(MediaPlayer.OnPreparedListener listener, Call call,
                            MediaDestroyer mediaDestroyer, MediaPlayer mediaPlayer) {
        this.listener = listener;
        this.call = call;
        this.mediaDestroyer = mediaDestroyer;
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public void execute(String password) {
        mediaDestroyer.destroyMedia(false);

        byte[] callData = FileCallDao.getInstance().getDecryptedCall(call, Application.getInstance().getAuthMan());

        String base64EncodedString = Base64.encodeToString(callData, Base64.DEFAULT);
        try {
            String url = "data:audio/amr;base64," + base64EncodedString;
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(listener);
            mediaPlayer.prepareAsync();
        } catch(Exception e){
            Log.e(this.getClass().getSimpleName(), "Error during playing preparing MediaPlayer", e);
            Toast.makeText(Application.getInstance(),
                    Application.getInstance().getString(R.string.toast_media_player_failed_init),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
