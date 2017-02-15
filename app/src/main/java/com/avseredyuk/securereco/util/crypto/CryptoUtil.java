package com.avseredyuk.securereco.util.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenfer on 2/15/17.
 */
public class CryptoUtil {

    public static byte[] encryptAES(String password, byte[] input) {
        try {
            byte[] key = HashingUtil.hashPassword(password);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(input);
        } catch (Exception e) {
            // todo
            throw new IllegalArgumentException(e);
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        return kpg.genKeyPair();
    }
}
