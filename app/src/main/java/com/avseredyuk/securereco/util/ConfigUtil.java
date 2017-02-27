package com.avseredyuk.securereco.util;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.avseredyuk.securereco.exception.CryptoException;
import com.avseredyuk.securereco.util.crypto.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;

import javax.crypto.Cipher;

import static com.avseredyuk.securereco.util.Constant.*;

/**
 * Created by lenfer on 2/15/17.
 */
public class ConfigUtil {

    public static boolean isKeysPresent() {
        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/");
        if (sampleDir.exists()) {
            File configFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + CONFIG_FILE);
            if (!configFile.isDirectory()) {
                return (!"".equals(readValue(PUBLIC_KEY))
                        && !"".equals(readValue(PRIVATE_KEY_ENCODED))
                        && !"".equals(readValue(PRIVATE_KEY_HMAC)));
            }
        }
        return false;
    }

    public static String readValue(String key)  {
        File configFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + CONFIG_FILE);
        if (!configFile.isDirectory()) {
            try {
                InputStream in = new FileInputStream(configFile);
                JSONObject json = new JSONObject(IOUtil.readText(in, "UTF-8"));
                return json.getString(key);
            } catch (JSONException e) {
                Log.e(ConfigUtil.class.getSimpleName(),
                        "Exception at reading JSON parameters", e);
            } catch (IOException e) {
                Log.e(ConfigUtil.class.getSimpleName(),
                        "Exception at reading config file", e);
            }
        }
        return "";
    }

    public static void makeKeys(String password) {
        try {
            KeyPair keyPair = RSA.generateKeyPair();

            AES aes = new AES();
            aes.init(password, Cipher.ENCRYPT_MODE);
            byte[] privateKeyEncrypted = aes.doFinal(keyPair.getPrivate().getEncoded());
            byte[] hmac = aes.getHMAC(keyPair.getPrivate().getEncoded());
            byte[] iv = aes.getCipher().getIV();

            File configFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + CONFIG_FILE);
            OutputStream out = new FileOutputStream(configFile);

            JSONObject json = new JSONObject();
            json.put(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncrypted, Base64.DEFAULT));
            json.put(PRIVATE_KEY_HMAC, Base64.encodeToString(hmac, Base64.DEFAULT));
            json.put(PRIVATE_KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT));
            json.put(PUBLIC_KEY, Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT));
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            out.close();

        } catch (CryptoException e) {
            Log.e(ConfigUtil.class.getSimpleName(),
                    "Exception at crypto stuff", e);
        } catch (JSONException e) {
            Log.e(ConfigUtil.class.getSimpleName(),
                    "Exception at saving JSON parameters", e);
        } catch (IOException e) {
            Log.e(ConfigUtil.class.getSimpleName(),
                    "Exception at writing to output stream to config file", e);
        }
    }

}
