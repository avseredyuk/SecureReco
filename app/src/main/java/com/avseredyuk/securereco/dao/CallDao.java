package com.avseredyuk.securereco.dao;

import android.os.Environment;
import android.util.Log;

import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.IOUtil;
import com.avseredyuk.securereco.util.StringUtil;
import com.avseredyuk.securereco.util.crypto.AES;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.CipherInputStream;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/16/17.
 */
public class CallDao {

    //public int getCount() {}

    public List<Call> findAllSortedByDate() {
        List<Call> calls = new ArrayList<>();
        File callFolder = new File(Environment.getExternalStorageDirectory(), "/" + CALL_LOGS_DIRECTORY + "/");

        for (final File fileEntry : callFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {

                try {
                    Call call = parseCallRecord(fileEntry);
                    calls.add(call);
                } catch (IllegalArgumentException e) {
                    // todo smth
                }
            }
        }
        Collections.sort(calls, Call.CallDateComparator);
        return calls;
    }

    private Call parseCallRecord(File file) throws IllegalArgumentException {
        Call call = StringUtil.getCallFromFilename(file.getName());

        byte[] content;
        byte[] iv = new byte[16];
        byte[] key = new byte[32];
        byte[] buf = new byte[BUF_SIZE];

        try {
            content = IOUtil.readFile(file);

            File yourFile = new File("/storage/emulated/0/SecureRecoApp/file.amr");

            try {
                InputStream is = new ByteArrayInputStream(content);
                is.read(key);
                is.read(iv);
                AES aes = new AES();
                if (!aes.initWithKeyAndIV(key, iv)) {
                    //todo
                    return null;
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

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return call;

    }
}
