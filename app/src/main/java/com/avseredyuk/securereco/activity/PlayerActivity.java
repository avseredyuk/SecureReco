package com.avseredyuk.securereco.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import com.avseredyuk.securereco.R;
import static com.avseredyuk.securereco.util.Constant.CALL_DECRYPTED_INTENT_DATA_NAME;

/**
 * Created by Anton_Serediuk on 5/31/2017.
 */

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        byte[] callData = getIntent().getByteArrayExtra(CALL_DECRYPTED_INTENT_DATA_NAME);

        String base64EncodedString = Base64.encodeToString(callData, Base64.DEFAULT);

        try
        {
            String url = "data:audio/amr;base64,"+base64EncodedString;
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch(Exception ex){
            System.out.print(ex.getMessage());
        }

    }
}
