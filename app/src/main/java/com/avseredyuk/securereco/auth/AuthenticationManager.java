package com.avseredyuk.securereco.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.avseredyuk.securereco.application.Application;
import com.avseredyuk.securereco.exception.AuthenticationException;
import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.service.BackgroundWorkIntentService;
import com.avseredyuk.securereco.util.ConfigUtil;
import com.avseredyuk.securereco.util.crypto.AES;
import com.avseredyuk.securereco.util.crypto.HMAC;
import com.avseredyuk.securereco.util.crypto.RSA;

import java.security.KeyPair;
import java.util.Arrays;

import javax.crypto.Cipher;

import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_CHANGE_FOLDER;
import static com.avseredyuk.securereco.util.Constant.BWIS_DESTINATION_REGENERATE_KEYS;
import static com.avseredyuk.securereco.util.Constant.BWIS_ACTION;
import static com.avseredyuk.securereco.util.Constant.CALL_DIR;
import static com.avseredyuk.securereco.util.Constant.OLD_FOLDER_PATH;
import static com.avseredyuk.securereco.util.Constant.OLD_PRIVATE_KEY_INTENT_EXTRA_NAME;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_ENCODED;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_HMAC;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_IV;
import static com.avseredyuk.securereco.util.Constant.PUBLIC_KEY;

/**
 * Created by lenfer on 2/27/17.
 */
public class AuthenticationManager {
    private byte[] privateKey;

    private AuthenticationManager() {
    }

    public static AuthenticationManager newAuthManInitialKeyGenWithAuthentication(String password) throws AuthenticationException {
        return new AuthenticationManager().createKeys(password).authenticate(password);
    }

    public static AuthenticationManager newAuthManWithAuthentication(String password) throws AuthenticationException {
        return new AuthenticationManager().authenticate(password);
    }

    public AuthenticationManager setAsApplicationAuthenticationManager() {
        Application.getInstance().setAuthMan(this);
        return this;
    }

    private AuthenticationManager authenticate(String password) throws AuthenticationException {
        try {
            byte[] hmacFromConfig = Base64.decode(ConfigUtil.readValue(PRIVATE_KEY_HMAC), Base64.DEFAULT);

            byte[] privateKeyIV = Base64.decode(ConfigUtil.readValue(PRIVATE_KEY_IV), Base64.DEFAULT);
            AES aes = new AES();
            aes.init(password, Cipher.DECRYPT_MODE, privateKeyIV);
            byte[] privateKeyEncoded = Base64.decode(ConfigUtil.readValue(PRIVATE_KEY_ENCODED), Base64.DEFAULT);
            privateKey = aes.doFinal(privateKeyEncoded);
            byte[] hmacFromPassword = HMAC.makeHMAC(aes.getKeyCipherTuple().getKey(), privateKey);

            if (!Arrays.equals(hmacFromConfig, hmacFromPassword)) {
                throw new AuthenticationException("Exception during authentication");
            }

        } catch (CryptoException e) {
            Log.d(getClass().getSimpleName(),
                    "CryptoException during authentication", e);
            throw new AuthenticationException("Exception during authentication");
        }
        return this;
    }

    private AuthenticationManager createKeys(String password) {
        try {
            KeyPair keyPair = RSA.generateKeyPair();

            privateKey = keyPair.getPrivate().getEncoded();
            AES aes = new AES();
            aes.init(password, Cipher.ENCRYPT_MODE, null);
            byte[] privateKeyEncoded = aes.doFinal(privateKey);
            byte[] hmacFromPassword = HMAC.makeHMAC(aes.getKeyCipherTuple().getKey(), privateKey);
            byte[] privateKeyIV = aes.getKeyCipherTuple().getCipher().getIV();

            ConfigUtil.writeValue(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_HMAC, Base64.encodeToString(hmacFromPassword, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_IV, Base64.encodeToString(privateKeyIV, Base64.DEFAULT));

            ConfigUtil.writeValue(PUBLIC_KEY, Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT));
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(), "Exception at crypto stuff", e);
        }
        return this;
    }

    public boolean regenerateKeyPair(Context context, String password) {
        byte[] oldPrivateKey = this.privateKey;
        createKeys(password);
        context.startService(new Intent(context, BackgroundWorkIntentService.class)
                .putExtra(BWIS_ACTION, BWIS_DESTINATION_REGENERATE_KEYS)
                .putExtra(OLD_PRIVATE_KEY_INTENT_EXTRA_NAME, oldPrivateKey));
        return true;
    }

    public boolean changeFolder(Context context, String newFolder) {
        String oldCallDir = ConfigUtil.readValue(CALL_DIR);
        ConfigUtil.writeValue(CALL_DIR, newFolder);
        context.startService(new Intent(context, BackgroundWorkIntentService.class)
                .putExtra(BWIS_ACTION, BWIS_DESTINATION_CHANGE_FOLDER)
                .putExtra(OLD_FOLDER_PATH, oldCallDir)
        );
        return true;
    }

    public boolean changePassword(String newPassword) {
        try {
            AES aes = new AES();
            aes.init(newPassword, Cipher.ENCRYPT_MODE, null);
            byte[] privateKeyEncoded = aes.doFinal(privateKey);
            byte[] hmacFromPassword = HMAC.makeHMAC(aes.getKeyCipherTuple().getKey(), privateKey);
            byte[] privateKeyIV = aes.getKeyCipherTuple().getCipher().getIV();

            ConfigUtil.writeValue(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_HMAC, Base64.encodeToString(hmacFromPassword, Base64.DEFAULT));
            ConfigUtil.writeValue(PRIVATE_KEY_IV, Base64.encodeToString(privateKeyIV, Base64.DEFAULT));
            return true;
        } catch (CryptoException e) {
            Log.e(getClass().getSimpleName(), "Exception changing password", e);
        }
        return false;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

}
