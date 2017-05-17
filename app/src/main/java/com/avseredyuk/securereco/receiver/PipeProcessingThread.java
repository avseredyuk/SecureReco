package com.avseredyuk.securereco.receiver;

import android.util.Log;

import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.util.ArrayUtil;
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
    private final InputStream in;
    private final FileOutputStream out;

    PipeProcessingThread(InputStream in, FileOutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            RSA rsa = new RSA();
            rsa.initPublicKey();
            AES aes = new AES();
            aes.initEncryptWithRandom();

            byte[] buf = new byte[BUF_SIZE];
            int len;

            out.write(rsa.doFinal(ArrayUtil.combineArrays(aes.getKey(), aes.getCipher().getIV())));

            CipherOutputStream outCipher = new CipherOutputStream(out, aes.getCipher());
            while ((len = in.read(buf)) > 0) {
                outCipher.write(buf, 0, len);
            }
            outCipher.close();
            in.close();

        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception writing from pool to file", e);
        }
    }
}
