package com.avseredyuk.securereco.dao;

import android.os.Environment;

import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.AuthenticationUtil;
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
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.CipherInputStream;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/16/17.
 */
public class CallDao {
    private static CallDao instance = new CallDao();

    private CallDao() {
    }

    public static CallDao getInstance() {
        return instance;
    }

    //public int getCount() {}

    public List<Call> findAll(Comparator<Call> comparator) {
        List<Call> calls = findAll();
        Collections.sort(calls, comparator);
        return calls;
    }

    public List<Call> findAll() {
        List<Call> calls = new ArrayList<>();
        File callFolder = new File(Environment.getExternalStorageDirectory(), "/" + CALL_LOGS_DIRECTORY + "/");

        for (final File fileEntry : callFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {

                try {
                    Call call = parseCallFromFilename(fileEntry.getName());
                    calls.add(call);
                } catch (IllegalArgumentException e) {
                    // todo smth
                }
            }
        }
        return calls;
    }

    private Call parseCallFromFilename(String filename)  {
        return StringUtil.getCallFromFilename(filename);
    }

    //TODO player ???
    public boolean play(Call call, String password) throws IllegalArgumentException {
        byte[] content;
        byte[] iv = new byte[16];
        byte[] key = new byte[32];
        byte[] headerEncrypted = new byte[128];
        byte[] buf = new byte[BUF_SIZE];
        byte[] privateKey;

        try {
            privateKey = AuthenticationUtil.authenticate(password);
            RSA rsa = new RSA();
            rsa.initPrivateKey(privateKey);

            content = IOUtil.readFile(call.getFilename());

            File yourFile = new File("/storage/emulated/0/SecureRecoApp/file.amr");

            try {
                InputStream is = new ByteArrayInputStream(content);
                is.read(headerEncrypted);
                byte[] fileHeader = rsa.doFinal(headerEncrypted);
                key = Arrays.copyOfRange(fileHeader, 0, key.length);
                iv = Arrays.copyOfRange(fileHeader, key.length, key.length + iv.length);

                AES aes = new AES();
                if (!aes.initWithKeyAndIV(key, iv)) {
                    //todo
                    //return null;
                    return false;
                }

                OutputStream out = new FileOutputStream(yourFile);
                CipherInputStream inCipher = new CipherInputStream(is, aes.getCipher());

                int numRead = 0;
                while ((numRead = inCipher.read(buf)) >= 0) {
                    out.write(buf, 0, numRead);
                }

                inCipher.close();
                out.close();
            } catch (Exception e) {
                //todo
                e.printStackTrace();
            }
        } catch (InvalidKeyException e) {
            return false;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return true;
    }
}
