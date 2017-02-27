package com.avseredyuk.securereco.util;

import android.util.Base64;

import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.util.crypto.AES;

import java.util.Arrays;

import javax.crypto.Cipher;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/27/17.
 */
public class AuthenticationUtil {
    public static byte[] authenticate(String password) throws CryptoException {
        String hmacFromConfigString = ConfigUtil.readValue(PRIVATE_KEY_HMAC);
        byte[] hmacFromConfig = Base64.decode(hmacFromConfigString, Base64.DEFAULT);

        String privateKeyEncodedString = ConfigUtil.readValue(PRIVATE_KEY_ENCODED);
        byte[] privateKeyEncoded = Base64.decode(privateKeyEncodedString, Base64.DEFAULT);

        String privateKeyIVString = ConfigUtil.readValue(PRIVATE_KEY_IV);
        byte[] privateKeyIV = Base64.decode(privateKeyIVString, Base64.DEFAULT);

        AES aes = new AES();
        aes.init(password, Cipher.DECRYPT_MODE, privateKeyIV);

        byte[] privateKey = aes.doFinal(privateKeyEncoded);
        byte[] hmacFromPassword = aes.getHMAC(privateKey);

        if (Arrays.equals(hmacFromConfig, hmacFromPassword)) {
            return privateKey;
        } else {
            throw new CryptoException("Exception at crypto stuff at authentication");
        }
    }
}
