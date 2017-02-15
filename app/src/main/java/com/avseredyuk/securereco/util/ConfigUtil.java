package com.avseredyuk.securereco.util;

import android.os.Environment;

import com.avseredyuk.securereco.util.crypto.CryptoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class ConfigUtil {

    public static boolean isKeysPresent() {
        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/");
        if (sampleDir.exists()) {
            File privateKeyFile = new File(sampleDir, PRIVATE_KEY_FILENAME);
            File publicKeyFile = new File(sampleDir, PUBLIC_KEY_FILENAME);
            if(privateKeyFile.exists() && !privateKeyFile.isDirectory() &&
                    publicKeyFile.exists() && !publicKeyFile.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    public static void makeKeys(String password) {
        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            byte[] privateKeyEncrypted = CryptoUtil.encryptAES(password, privateKey.getEncoded());

            File privateKeyFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + PRIVATE_KEY_FILENAME);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
            oos.writeObject(privateKeyEncrypted);
            oos.close();

            File publicKeyFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + PUBLIC_KEY_FILENAME);
            oos = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
            oos.writeObject(publicKey);
            oos.close();

        } catch (NoSuchAlgorithmException e) {
            // todo log
        } catch (IOException e) {
            //todo log
        }
    }

}
