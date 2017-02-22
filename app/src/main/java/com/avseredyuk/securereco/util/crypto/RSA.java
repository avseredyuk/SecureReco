package com.avseredyuk.securereco.util.crypto;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/16/17.
 */
public class RSA {
    private PublicKey publicKey;

    public boolean initPublicKey() {
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        File publicKeyFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + PUBLIC_KEY_FILENAME);
        try {
            fin = new FileInputStream(publicKeyFile);
            ois = new ObjectInputStream(fin);
            publicKey = (PublicKey) ois.readObject();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        return kpg.genKeyPair();
    }


}
