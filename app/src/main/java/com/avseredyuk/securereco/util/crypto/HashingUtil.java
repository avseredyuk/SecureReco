package com.avseredyuk.securereco.util.crypto;

import com.avseredyuk.securereco.exception.CryptoException;

import java.nio.charset.StandardCharsets;
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
            md.update(in.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }
}
