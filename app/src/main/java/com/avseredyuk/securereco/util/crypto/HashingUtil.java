package com.avseredyuk.securereco.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lenfer on 2/15/17.
 */
public class HashingUtil {

    private HashingUtil() {
    }

    public static byte[] hashPassword(String in) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(in.getBytes(StandardCharsets.UTF_8));
        byte[] out = md.digest();
        return out;
    }
}
