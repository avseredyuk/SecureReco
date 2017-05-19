package com.avseredyuk.securereco.util.crypto;

import com.avseredyuk.securereco.exception.CryptoException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * Created by Anton_Serediuk on 5/19/2017.
 */

public class HMAC {
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private HMAC() {
    }

    public static byte[] makeHMAC(SecretKey secretKey, byte[] input) throws CryptoException{
        try {
            Mac HMAC = Mac.getInstance(HMAC_SHA_256);
            HMAC.init(secretKey);
            return HMAC.doFinal(input);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CryptoException("Exception getting HMAC", e);
        }
    }

}
