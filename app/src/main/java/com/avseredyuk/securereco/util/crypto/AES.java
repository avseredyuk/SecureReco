package com.avseredyuk.securereco.util.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenfer on 2/16/17.
 */
public class AES {
    private SecretKey secretKey;
    private Cipher cipher;
    private Mac HMAC;

    public boolean initRandom(boolean isEncrypting) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            secretKey = keyGen.generateKey();
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            int opMode = isEncrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, secretKey);
            return true;
        } catch (Exception e) {
            //todo
        }
        return false;
    }

    public boolean initWithKeyAndIV(byte[] key, byte[] iv) {
        try {
            secretKey = new SecretKeySpec(key, 0, key.length, "AES");
            cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return true;
        } catch (Exception e) {
            //todo
        }
        return false;
    }

    public boolean initWithPassword(String password, int opMode) {
        try {
            byte[] key = HashingUtil.hashPassword(password);
            HMAC = Mac.getInstance("HmacSHA256");
            secretKey = new SecretKeySpec(key, "HmacSHA256");
            HMAC.init(secretKey);
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(opMode, secretKey);
            return true;
        } catch (Exception e) {
            //todo
        }
        return false;
    }

    public boolean initWithPassword(String password, int opMode, byte[] iv) {
        try {
            byte[] key = HashingUtil.hashPassword(password);
            HMAC = Mac.getInstance("HmacSHA256");
            secretKey = new SecretKeySpec(key, "HmacSHA256");
            HMAC.init(secretKey);
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(opMode, secretKey, new IvParameterSpec(iv));
            return true;
        } catch (Exception e) {
            //todo
        }
        return false;
    }

    public byte[] doFinal(byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (Exception e) {
            // todo
            throw new IllegalArgumentException(e);
        }
    }

    public byte[] getHMAC(byte[] input) {
        if (HMAC != null) {
            return HMAC.doFinal(input);
        } else {
            //todo
            throw new IllegalArgumentException();
        }
    }

    public byte[] getKey() {
        return secretKey.getEncoded();
    }

    public Cipher getCipher() {
        return cipher;
    }
}
