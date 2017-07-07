package com.avseredyuk.securereco.util.crypto;

import android.util.Log;

import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.exception.CryptoException;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by lenfer on 2/16/17.
 */
public class RSA {
    private static final String RSA = "RSA";

    public static KeyPair generateKeyPair() throws CryptoException {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(1024);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Log.e(RSA.getClass().getSimpleName(),
                    "Exception while generating key pair", e);
            throw new CryptoException(e);
        }
    }

    public static Cipher getPublicKeyCipher() throws CryptoException{
        byte[] keyBaseEncoded = Application.getInstance().getConfiguration().getPublicKey();
        try {
            PublicKey publicKey = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(keyBaseEncoded));
            Cipher localCipher = Cipher.getInstance(RSA);
            localCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return localCipher;
        } catch (Exception e) {
            Log.e(RSA.getClass().getSimpleName(),
                    "Exception at getPublicKeyCipher", e);
            throw new CryptoException(e);
        }
    }

    public static Cipher getPrivateKeyCipher(byte[] key) throws CryptoException{
        try {
            PrivateKey privateKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(key));
            Cipher localCipher = Cipher.getInstance(RSA);
            localCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return localCipher;
        } catch (Exception e) {
            Log.e(RSA.getClass().getSimpleName(),
                    "Exception at getPrivateKeyCipher", e);
            throw new CryptoException(e);
        }
    }
}
