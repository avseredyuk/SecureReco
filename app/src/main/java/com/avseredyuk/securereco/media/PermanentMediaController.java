package com.avseredyuk.securereco.media;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by lenfer on 2/24/18.
 */

public class PermanentMediaController extends MediaController {
    private final MediaDestroyer mediaDestroyer;

    public PermanentMediaController(Context context, MediaDestroyer mediaDestroyer) {
        super(context);
        this.mediaDestroyer = mediaDestroyer;
    }

    @Override
    public void show(int timeout) {
        super.show(0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            mediaDestroyer.destroyMedia(true);
        }
        return super.dispatchKeyEvent(event);
    }
}