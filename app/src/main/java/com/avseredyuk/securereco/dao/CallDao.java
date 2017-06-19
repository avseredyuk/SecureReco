package com.avseredyuk.securereco.dao;

import android.util.Log;

import com.avseredyuk.securereco.auth.AuthenticationManager;
import com.avseredyuk.securereco.callback.FileCallback;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.exception.ParserException;
import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.ContactResolverUtil;
import com.avseredyuk.securereco.util.IOUtil;
import com.avseredyuk.securereco.util.StringUtil;
import com.avseredyuk.securereco.util.crypto.AES;
import com.avseredyuk.securereco.util.crypto.RSA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

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
        final List<Call> calls = new ArrayList<>();
        FileCallback callback = new FileCallback() {
            @Override
            public void execute(File file) {
                if (!file.isDirectory() && file.getName().endsWith(".bin")) {
                    try {
                        Call call = StringUtil.getCallFromFilename(file.getName());
                        call.setContactName(
                                ContactResolverUtil.getContactName(call.getCallNumber())
                        );
                        calls.add(call);
                    } catch (ParserException e) {
                        Log.e(getClass().getSimpleName(),
                                "Exception at parsing call filename", e);
                    }
                }
            }
        };
        IOUtil.processFiles(ConfigUtil.getCallLogsDir(), callback);
        return calls;
    }

    public boolean delete(Call call) {
        File file = new File(call.getFilename());
        return file.delete();
    }

    public boolean moveFromTempToPermanentFile(Call call) {
        File sampleDir = new File(ConfigUtil.getCallLogsDir());
        File from = new File(sampleDir, StringUtil.formatFileName(call, true));
        File to = new File(sampleDir, StringUtil.formatFileName(call, false));
        return from.renameTo(to);
    }

    public File createTemporaryFile(Call call) {
        File sampleDir = new File(ConfigUtil.getCallLogsDir());
        sampleDir.mkdirs();
        return new File(sampleDir, StringUtil.formatFileName(call, true));
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

    public boolean exportDecryptedCall(String filePath, Call call, AuthenticationManager authMan) {
        byte[] callData = getDecryptedCall(call, authMan);
        if (callData.length > 0) {
            try {
                return IOUtil.writeFile(filePath, callData);
            } catch (IOException e) {
                //todo
            }
        }
        return false;
    }

    public byte[] getDecryptedCall(Call call, AuthenticationManager authMan)  {
        byte[] fileByteArray;
        byte[] iv = new byte[16];
        byte[] key = new byte[32];
        byte[] headerEncrypted = new byte[ENCRYPTED_HEADER_SIZE];

        InputStream byteInputStream = null;
        CipherInputStream cipherInputStream = null;
        try {
            Cipher rsaCipher = RSA.getPrivateKeyCipher(authMan.getPrivateKey());
            fileByteArray = IOUtil.readFile(call.getFilename());
            byteInputStream = new ByteArrayInputStream(fileByteArray);
            if (byteInputStream.read(headerEncrypted) == headerEncrypted.length) {
                byte[] fileHeader = rsaCipher.doFinal(headerEncrypted);
                key = Arrays.copyOfRange(fileHeader, 0, key.length);
                iv = Arrays.copyOfRange(fileHeader, key.length, key.length + iv.length);
                cipherInputStream = new CipherInputStream(byteInputStream, AES.initDecrypt(key, iv));
                return IOUtil.inputStreamToByteArray(cipherInputStream);
            }
        } catch (GeneralSecurityException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at CallDao.getDecryptedCall() stuff", e);
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at playing decrypted call file", e);
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (cipherInputStream != null) {
                    cipherInputStream.close();
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(),
                        "Exception at close stream while playing call file", e);
            }
        }
        return new byte[0];
    }
}
