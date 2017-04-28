package com.avseredyuk.securereco.util.crypto;

import com.avseredyuk.securereco.exception.CryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenfer on 2/16/17.
 */
public class AES {
    public static final String HMAC_SHA_256 = "HmacSHA256";
    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String algorithmAES = "AES";
    private SecretKey secretKey;
    private Cipher cipher;
    private Mac HMAC;

    public void initRandom(boolean isEncrypting) throws CryptoException{
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithmAES);
            keyGen.init(256);
            secretKey = keyGen.generateKey();
            cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            int opMode = isEncrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(opMode, secretKey);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public void init(byte[] key, byte[] iv) throws CryptoException{
        try {
            secretKey = new SecretKeySpec(key, 0, key.length, algorithmAES);
            cipher = Cipher.getInstance (AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public void init(String password, int opMode) throws CryptoException{
        try {
            byte[] key = HashingUtil.hashPassword(password);
            HMAC = Mac.getInstance(HMAC_SHA_256);
            secretKey = new SecretKeySpec(key, HMAC_SHA_256);
            HMAC.init(secretKey);
            cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(opMode, secretKey);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public void init(String password, int opMode, byte[] iv) throws CryptoException{
        try {
            byte[] key = HashingUtil.hashPassword(password);
            HMAC = Mac.getInstance(HMAC_SHA_256);
            secretKey = new SecretKeySpec(key, HMAC_SHA_256);
            HMAC.init(secretKey);
            cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(opMode, secretKey, new IvParameterSpec(iv));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public byte[] doFinal(byte[] input) throws CryptoException{
        try {
            return cipher.doFinal(input);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] getHMAC(byte[] input) throws CryptoException{
        if (HMAC != null) {
            return HMAC.doFinal(input);
        } else {
            throw new CryptoException("Exception getting HMAC");
        }
    }

    public byte[] getKey() {
        return secretKey.getEncoded();
    }

    public Cipher getCipher() {
        return cipher;
    }
}
