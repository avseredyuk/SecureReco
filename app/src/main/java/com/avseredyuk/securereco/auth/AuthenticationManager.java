package com.avseredyuk.securereco.auth;

import android.util.Base64;
import android.util.Log;

import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.crypto.*;

import java.security.KeyPair;
import java.util.Arrays;

import javax.crypto.Cipher;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/27/17.
 */
public class AuthenticationManager {
    byte[] hmacFromConfig;
    byte[] privateKeyEncoded;
    byte[] privateKeyIV;
    byte[] privateKey;
    byte[] hmacFromPassword;
    KeyPair keyPair;

    private void initAuth(String password) throws CryptoException {
        String hmacFromConfigString = ConfigUtil.readValue(PRIVATE_KEY_HMAC);
        hmacFromConfig = Base64.decode(hmacFromConfigString, Base64.DEFAULT);

        String privateKeyEncodedString = ConfigUtil.readValue(PRIVATE_KEY_ENCODED);
        privateKeyEncoded = Base64.decode(privateKeyEncodedString, Base64.DEFAULT);

        String privateKeyIVString = ConfigUtil.readValue(PRIVATE_KEY_IV);
        privateKeyIV = Base64.decode(privateKeyIVString, Base64.DEFAULT);

        AES aes = new AES();
        aes.init(password, Cipher.DECRYPT_MODE, privateKeyIV);

        privateKey = aes.doFinal(privateKeyEncoded);
        hmacFromPassword = aes.getHMAC(privateKey);
    }

    private void protectPrivateKey(String password, byte[] localPrivateKey) throws CryptoException {
        AES aes = new AES();
        aes.init(password, Cipher.ENCRYPT_MODE);
        privateKeyEncoded = aes.doFinal(localPrivateKey);
        hmacFromPassword = aes.getHMAC(localPrivateKey);
        privateKeyIV = aes.getCipher().getIV();
    }

    public void makeKeys(String password) {
        try {
            keyPair = RSA.generateKeyPair();

            protectPrivateKey(password, keyPair.getPrivate().getEncoded());

            ConfigUtil.writeValue(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_HMAC, Base64.encodeToString(hmacFromPassword, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_IV, Base64.encodeToString(privateKeyIV, Base64.DEFAULT));
            ConfigUtil.writeValue(PUBLIC_KEY, Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT));
        } catch (CryptoException e) {
            Log.e(ConfigUtil.class.getSimpleName(), "Exception at crypto stuff", e);
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        try {
            initAuth(oldPassword);

            protectPrivateKey(newPassword, privateKey);

            ConfigUtil.writeValue(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_HMAC, Base64.encodeToString(hmacFromPassword, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_IV, Base64.encodeToString(privateKeyIV, Base64.DEFAULT));

            return true;
        } catch (CryptoException e) {
            Log.e(ConfigUtil.class.getSimpleName(), "Exception changing password", e);
        }
        return false;
    }

    public byte[] authenticate(String password) throws AuthenticationException {
        try {
            initAuth(password);

            if (Arrays.equals(hmacFromConfig, hmacFromPassword)) {
                return privateKey;
            } else {
                throw new AuthenticationException("Exception during authentication");
            }
        } catch (CryptoException e) {
            throw new AuthenticationException("Exception during authentication");
        }
    }
}
