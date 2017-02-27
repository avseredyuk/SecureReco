package com.avseredyuk.securereco.util.crypto;

import android.util.Base64;

import com.avseredyuk.securereco.util.ConfigUtil;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/16/17.
 */
public class RSA {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Cipher cipher;

    public boolean initPublicKey() {
        String keyStringBaseEncoded = ConfigUtil.readValue(PUBLIC_KEY);
        byte[] keyBaseEncoded = Base64.decode(keyStringBaseEncoded, Base64.DEFAULT);
        try {
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBaseEncoded));
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return true;
        } catch (InvalidKeySpecException e) {
            //todo
        } catch (NoSuchAlgorithmException e) {
            //todo
        } catch (NoSuchPaddingException e) {
            //todo
        } catch (InvalidKeyException e) {
            //todo
        }
        return false;
    }

    public boolean initPrivateKey(byte[] key) {
        try {
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return true;
        } catch (InvalidKeySpecException e) {
            //todo
        } catch (NoSuchAlgorithmException e) {
            //todo
        } catch (NoSuchPaddingException e) {
            //todo
        } catch (InvalidKeyException e) {
            //todo
        }
        return false;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        return kpg.genKeyPair();
    }

    public byte[] doFinal(byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (Exception e) {
            //todo
            throw new IllegalArgumentException(e);
        }
    }

}
