package com.avseredyuk.securereco.receiver;

import android.util.Log;

import com.avseredyuk.securereco.dao.CallDao;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ArrayUtil;
import com.avseredyuk.securereco.util.crypto.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class PipeProcessingThread extends Thread {
    private final InputStream in;
    private final Call call;

    PipeProcessingThread(Call call, InputStream in) {
        this.in = in;
        this.call = call;
    }

    @Override
    public void run() {
        CipherOutputStream outCipher = null;
        try {
            File callFile = CallDao.getInstance().createTemporaryFile(call);
            FileOutputStream out = new FileOutputStream(callFile);

            Cipher rsaCipher = RSA.getPublicKeyCipher();

            AES.KeyCipherTuple keyCipherTuple = AES.initEncryptWithRandom();
            byte[] buf = new byte[BUF_SIZE];
            int len;

            out.write(rsaCipher.doFinal(ArrayUtil.combineArrays(keyCipherTuple.getKey().getEncoded(),
                    keyCipherTuple.getCipher().getIV())));

            outCipher = new CipherOutputStream(out, keyCipherTuple.getCipher());
            while ((len = in.read(buf)) > 0) {
                outCipher.write(buf, 0, len);
            }

        } catch (GeneralSecurityException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at PipeProcessingThread.run() stuff", e);
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception writing from pool to file", e);
        } finally {
            try {
                if (outCipher != null) {
                    outCipher.close();
                }
            } catch (IOException e) {
                //todo
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                //todo
            }

            // move call log file from temporary to permanent file name
            call.setDateTimeEnded(new Date());
            CallDao.getInstance().moveFromTempToPermanentFile(call);
        }
    }
}
