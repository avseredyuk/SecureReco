package com.avseredyuk.securereco.util.crypto;

import android.util.Base64;

import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.util.ConfigUtil;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/16/17.
 */
public class RSA {
    private Cipher cipher;

    public void initPublicKey() throws CryptoException{
        String keyStringBaseEncoded = ConfigUtil.readValue(PUBLIC_KEY);
        byte[] keyBaseEncoded = Base64.decode(keyStringBaseEncoded, Base64.DEFAULT);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBaseEncoded));
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public void initPrivateKey(byte[] key) throws CryptoException{
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static KeyPair generateKeyPair() throws CryptoException {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

    }

    public byte[] doFinal(byte[] input) throws CryptoException{
        try {
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

}
