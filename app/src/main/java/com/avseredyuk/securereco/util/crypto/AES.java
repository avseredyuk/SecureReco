package com.avseredyuk.securereco.util.crypto;

import android.util.Log;

import com.avseredyuk.securereco.exception.CryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lenfer on 2/16/17.
 */
public class AES {
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String algorithmAES = "AES";
    private KeyCipherTuple keyCipherTuple;

    public static class KeyCipherTuple {
        public final SecretKey secretKey;
        public final Cipher cipher;
        public KeyCipherTuple(SecretKey secretKey, Cipher cipher) {
            this.secretKey = secretKey;
            this.cipher = cipher;
        }
        public SecretKey getKey() {
            return secretKey;
        }
        public Cipher getCipher() {
            return cipher;
        }
    }

    public static KeyCipherTuple initEncryptWithRandom() throws CryptoException{
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithmAES);
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            int opMode = Cipher.ENCRYPT_MODE;
            cipher.init(opMode, secretKey);
            return new KeyCipherTuple(secretKey, cipher);
        } catch (Exception e) {
            Log.e(AES.class.getSimpleName(),
                    "Exception at AES.initEncryptWithRandom", e);
            throw new CryptoException(e);
        }
    }

    public static Cipher initDecrypt(byte[] key, byte[] iv) throws CryptoException{
        try {
            SecretKey localSecretKey = new SecretKeySpec(key, 0, key.length, algorithmAES);
            Cipher localCipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            localCipher.init(Cipher.DECRYPT_MODE, localSecretKey, new IvParameterSpec(iv));
            return localCipher;
        } catch (Exception e) {
            Log.e(AES.class.getSimpleName(),
                    "Exception at AES.initDecrypt(byte[], byte[])", e);
            throw new CryptoException(e);
        }
    }

    public void init(String password, int opMode, byte[] iv) throws CryptoException{
        try {
            byte[] key = HashingUtil.hashPassword(password);
            SecretKey localSecretKey = new SecretKeySpec(key, HMAC_SHA_256);
            Cipher localCipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            if (iv != null) {
                localCipher.init(opMode, localSecretKey, new IvParameterSpec(iv));
            } else {
                localCipher.init(opMode, localSecretKey);
            }
            keyCipherTuple = new KeyCipherTuple(localSecretKey, localCipher);

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at AES.init()", e);
            throw new CryptoException(e);
        }
    }

    public byte[] doFinal(byte[] input) throws CryptoException{
        try {
            return getKeyCipherTuple().getCipher().doFinal(input);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception at doFinal", e);
            throw new CryptoException(e);
        }
    }

    public KeyCipherTuple getKeyCipherTuple() {
        return keyCipherTuple;
    }
}
