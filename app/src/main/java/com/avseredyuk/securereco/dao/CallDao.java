package com.avseredyuk.securereco.dao;

import android.os.Environment;
import android.util.Log;

import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.exception.ParserException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.IOUtil;
import com.avseredyuk.securereco.util.StringUtil;
import com.avseredyuk.securereco.util.crypto.AES;
import com.avseredyuk.securereco.util.crypto.RSA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.CipherInputStream;

import static com.avseredyuk.securereco.util.Constant.BUF_SIZE;
import static com.avseredyuk.securereco.util.Constant.CALL_LOGS_DIRECTORY;

/**
 * Created by lenfer on 2/16/17.
 */
public class CallDao {
    private static final CallDao instance = new CallDao();

    private CallDao() {
    }

    public static CallDao getInstance() {
        return instance;
    }

    public List<Call> findAll(Comparator<Call> comparator) {
        List<Call> calls = findAll();
        Collections.sort(calls, comparator);
        return calls;
    }

    public List<Call> findAll() {
        List<Call> calls = new ArrayList<>();
        File callFolder = new File(Environment.getExternalStorageDirectory(), "/" + CALL_LOGS_DIRECTORY + "/");
        if (callFolder.listFiles() != null) {
            for (final File fileEntry : callFolder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    try {
                        Call call = StringUtil.getCallFromFilename(fileEntry.getName());
                        calls.add(call);
                    } catch (ParserException e) {
                        Log.e(getClass().getSimpleName(),
                                "Exception at parsing call filename", e);
                    }
                }
            }
        }
        return calls;
    }

    //TODO player ???
    public boolean play(Call call, AuthenticationManager authMan)  {
        byte[] content;
        byte[] iv = new byte[16];
        byte[] key = new byte[32];
        byte[] headerEncrypted = new byte[128];
        byte[] buf = new byte[BUF_SIZE];
        byte[] privateKey;

        try {
            privateKey = authMan.getPrivateKey();
            RSA rsa = new RSA();
            rsa.initPrivateKey(privateKey);

            content = IOUtil.readFile(call.getFilename());

            File yourFile = new File("/storage/emulated/0/SecureRecoApp/file.amr");

            InputStream is = new ByteArrayInputStream(content);
            is.read(headerEncrypted);
            byte[] fileHeader = rsa.doFinal(headerEncrypted);
            key = Arrays.copyOfRange(fileHeader, 0, key.length);
            iv = Arrays.copyOfRange(fileHeader, key.length, key.length + iv.length);

            AES aes = new AES();
            aes.init(key, iv);

            OutputStream out = new FileOutputStream(yourFile);
            CipherInputStream inCipher = new CipherInputStream(is, aes.getCipher());

            int numRead;
            while ((numRead = inCipher.read(buf)) >= 0) {
                out.write(buf, 0, numRead);
            }

            inCipher.close();
            out.close();

            return true;

        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at playing decrypted call file", e);
        }
        return false;
    }
}
