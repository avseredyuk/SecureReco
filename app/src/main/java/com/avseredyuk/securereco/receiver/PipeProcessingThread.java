package com.avseredyuk.securereco.receiver;

import android.util.Log;
import com.avseredyuk.securereco.util.crypto.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.crypto.CipherOutputStream;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class PipeProcessingThread extends Thread {
    InputStream in;
    FileOutputStream out;

    PipeProcessingThread(InputStream in, FileOutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        /*RSA rsa = new RSA();
        if (!rsa.initPublicKey()) {
            // todo
            return;
        }
        */
        AES aes = new AES();
        if (!aes.initRandom(true)) {
            // todo
            return;
        }

        byte[] buf = new byte[BUF_SIZE];
        int len;
        try {
            out.write(aes.getKey());
            out.write(aes.getCipher().getIV());

            CipherOutputStream outCipher = new CipherOutputStream(out, aes.getCipher());
            while ((len = in.read(buf)) > 0) {
                outCipher.write(buf, 0, len);
            }
            outCipher.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(getClass().getSimpleName(),
                    "Exception transferring file", e);
        }
    }
}
