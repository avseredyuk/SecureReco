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
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import static com.avseredyuk.securereco.util.Constant.BUF_SIZE;
import static com.avseredyuk.securereco.util.Constant.CALL_LOGS_DIRECTORY;

/**
 * Created by lenfer on 2/16/17.
 */
public class CallDao {
    private static final int ENCRYPTED_HEADER_SIZE = 128;
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

    public boolean reEncryptHeader(Call call, byte[] oldPrivateKey) {
        byte[] fileHeaderEncrypted = new byte[ENCRYPTED_HEADER_SIZE];
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(new File(call.getFilename()), "rw");
            f.seek(0);

            Cipher rsaDecryptCipher = RSA.getPrivateKeyCipher(oldPrivateKey);
            Cipher rsaEncryptCipher = RSA.getPublicKeyCipher();

            if (f.read(fileHeaderEncrypted) == fileHeaderEncrypted.length) {
                byte[] fileHeaderDecrypted = rsaDecryptCipher.doFinal(fileHeaderEncrypted);
                byte[] fileHeaderReEncrypted = rsaEncryptCipher.doFinal(fileHeaderDecrypted);
                f.seek(0);
                f.write(fileHeaderReEncrypted);
                return true;
            }
            return false;
        } catch (GeneralSecurityException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at CallDao.reEncryptHeader() stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception re-encrypting call file", e);
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(),
                            "Exception closing re-encrypted call file", e);
                }
            }
        }
        return false;
    }

    //TODO player ???
    public boolean play(Call call, AuthenticationManager authMan)  {
        byte[] fileByteArray;
        byte[] iv = new byte[16];
        byte[] key = new byte[32];
        byte[] headerEncrypted = new byte[ENCRYPTED_HEADER_SIZE];
        byte[] buf = new byte[BUF_SIZE];

        InputStream byteInputStream = null;
        CipherInputStream cipherInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            Cipher rsaCipher = RSA.getPrivateKeyCipher(authMan.getPrivateKey());

            fileByteArray = IOUtil.readFile(call.getFilename());

            File yourFile = new File("/storage/emulated/0/SecureRecoApp/file.amr");

            byteInputStream = new ByteArrayInputStream(fileByteArray);
            if (byteInputStream.read(headerEncrypted) == headerEncrypted.length) {
                byte[] fileHeader = rsaCipher.doFinal(headerEncrypted);
                key = Arrays.copyOfRange(fileHeader, 0, key.length);
                iv = Arrays.copyOfRange(fileHeader, key.length, key.length + iv.length);

                fileOutputStream = new FileOutputStream(yourFile);
                cipherInputStream = new CipherInputStream(byteInputStream, AES.initDecrypt(key, iv));

                int numRead;
                while ((numRead = cipherInputStream.read(buf)) >= 0) {
                    fileOutputStream.write(buf, 0, numRead);
                }

                return true;
            }
        } catch (GeneralSecurityException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at CallDao.play() stuff", e);
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at playing decrypted call file", e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (cipherInputStream != null) {
                    cipherInputStream.close();
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at close stream while plaing call file", e);
            }
        }
        return false;
    }
}
