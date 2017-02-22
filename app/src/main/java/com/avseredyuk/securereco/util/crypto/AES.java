package com.avseredyuk.securereco.util.crypto;

import android.util.Log;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenfer on 2/16/17.
 */
public class AES {
    private SecretKey secretKey;
    private Cipher cipher;

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
            e.printStackTrace();
        }
        return false;
    }

    public boolean initWithKeyAndIV(byte[] key, byte[] iv) {
        try {
            secretKey = new SecretKeySpec(key, 0, key.length, "AES");
            Cipher cipher = Cipher.getInstance ( "AES/CBC/PKCS5Padding" );
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return true;
        } catch (Exception e) {
            //todo
        }
        return false;
    }

    public byte[] getKey() {
        Log.d("KEYSIZE", Integer.toString(secretKey.getEncoded().length));
        return secretKey.getEncoded();
    }

    public Cipher getCipher() {
        return cipher;
    }

    public static byte[] encryptWithPassword(String password, byte[] input) {
        try {
            byte[] key = HashingUtil.hashPassword(password);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            localCipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return localCipher.doFinal(input);
        } catch (Exception e) {
            // todo
            throw new IllegalArgumentException(e);
        }
    }
}
