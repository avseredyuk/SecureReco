package com.avseredyuk.securereco.util.crypto;

import android.util.Log;

import com.avseredyuk.securereco.exception.CryptoException;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lenfer on 2/15/17.
 */
public class HashingUtil {

    private HashingUtil() {
    }

    public static byte[] hashPassword(String in) throws CryptoException{
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(in.getBytes(Charset.forName("UTF-8")));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(HashingUtil.class.getSimpleName(),
                    "Hash algo not found", e);
            throw new CryptoException(e);
        }
    }
}
