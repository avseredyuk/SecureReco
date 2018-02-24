package com.avseredyuk.securereco.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avseredyuk.securereco.R;
import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.callback.Callback;
import com.avseredyuk.securereco.callback.PlayCallCallback;
import com.avseredyuk.securereco.dao.FileCallDao;
import com.avseredyuk.securereco.dao.SQLiteCallDao;
import com.avseredyuk.securereco.media.MediaDestroyer;
import com.avseredyuk.securereco.media.PermanentMediaController;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ContactResolverUtil;
import com.avseredyuk.securereco.util.StringUtil;

import static com.avseredyuk.securereco.util.Constant.INTENT_EXTRA_CALL_DATA;

/**
 * Created by lenfer on 7/9/17.
 */

public class MoreActivity extends SecuredActivity
        implements MediaPlayer.OnPreparedListener, MediaDestroyer,
        MediaController.MediaPlayerControl {

    //todo: layout of this activity in landscape orientation is fucked-up
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final Handler handler = new Handler();
    private MediaController mediaController;
    private ImageView photoImageView;
    private Menu menu;
    private Call callToShow;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaController = new PermanentMediaController(this, this);

        setContentView(R.layout.activity_more);
        photoImageView = (ImageView) findViewById(R.id.photoImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        callToShow = getIntent().getParcelableExtra(INTENT_EXTRA_CALL_DATA);

        Bitmap highResPhotoBitmap = ContactResolverUtil.retrieveHighResContactPhoto(callToShow);
        if (highResPhotoBitmap != null) {
            photoImageView.setImageBitmap(highResPhotoBitmap);
        }

        ActionBar ab = getSupportActionBar();
        ab.setTitle(callToShow.getContactName());
        ab.setSubtitle(callToShow.getCallNumber());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.more_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showPlayerButtonClickListenerOnClick(View v) {
        Callback playCallCallback = new PlayCallCallback(MoreActivity.this, callToShow, MoreActivity.this, mediaPlayer);

        if (Application.getInstance().isAuthenticated()) {
            playCallCallback.execute(null);
        } else {
            makeAlertDialog(playCallCallback);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (callToShow.isStarred()) {
            menu.getItem(0).setIcon(R.drawable.ic_star_24dp);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_star_border_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_star:
                menuItemSetStar();
                break;
            case R.id.action_delete:
                menuItemDelete();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void menuItemSetStar() {
        callToShow.setStarred(!callToShow.isStarred());
        SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
        dao.updateStarredCall(callToShow);
        dao.close();

        menu.getItem(0).setIcon(callToShow.isStarred()
                        ? R.drawable.ic_star_24dp
                        : R.drawable.ic_star_border_24dp
        );
    }

    private void menuItemDelete() {
        SQLiteCallDao dao = new SQLiteCallDao(getApplicationContext()).open();
        if ((dao.delete(callToShow)) && (FileCallDao.getInstance().delete(callToShow))) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_record_deleted), Toast.LENGTH_SHORT).show();
        }
        dao.close();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void destroyMedia(boolean hideUi) {
        if (hideUi) {
            mediaController.hide();
        }
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException e) {
            Log.d(this.getClass().getSimpleName(), "Error destroying media", e);
        }
        mediaPlayer.reset();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.more_activity));
        handler.post(new Runnable() {
            public void run() {
                mediaPlayer.start();
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    @Override
    public void updateUIOnAuthenticationReset() {
        super.updateUIOnAuthenticationReset();
        destroyMedia(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyMedia(true);
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
