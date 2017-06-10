package com.avseredyuk.securereco.util;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static com.avseredyuk.securereco.util.Constant.APP_DIRECTORY;
import static com.avseredyuk.securereco.util.Constant.CONFIG_FILE;
import static com.avseredyuk.securereco.util.Constant.IS_ENABLED;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ON;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_ENCODED;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_HMAC;
import static com.avseredyuk.securereco.util.Constant.PUBLIC_KEY;
import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_STRATEGY;

/**
 * Created by lenfer on 2/15/17.
 */
public class ConfigUtil {

    private ConfigUtil() {
    }

    public static boolean isConfigValid() {
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

    public static void initDefaultConfiguration() {
        ConfigUtil.writeValue(NOTIFICATION_ON, Boolean.toString(true));
        ConfigUtil.writeValue(IS_ENABLED, Boolean.toString(true));
        ConfigUtil.writeValue(RESET_AUTH_STRATEGY,
                String.valueOf(ResetAuthenticationStrategy.WHEN_APP_GOES_TO_BACKGROUND.getValue()));
    }

    public static String readValue(String key)  {
        try {
            JSONObject json = readObject();
            if (json != null) {
                return (String) json.get(key);
            }
        } catch (JSONException e) {
            Log.d(ConfigUtil.class.getSimpleName(),
                    "Exception at parsing JSON parameters", e);
        }
        return "";
    }

    public static Boolean readBoolean(String key) {
        return Boolean.valueOf(readValue(key));
    }

    public static boolean writeBoolean(String name, Boolean value) {
        return writeValue(name, value.toString());
    }

    public static int readInt(String key) {
        return Integer.valueOf(readValue(key));
    }

    public static boolean writeValue(String name, String value) {
        JSONObject json = readObject();
        if (json == null) {
            json = new JSONObject();
        }

        File configFile = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY + "/" + CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        OutputStream out = null;
        try {
            out = new FileOutputStream(configFile);
            json.put(name, value);
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            return true;

        } catch (JSONException e) {
            Log.d(ConfigUtil.class.getSimpleName(),
                    "Exception at saving JSON parameters", e);
        } catch (IOException e) {
            Log.d(ConfigUtil.class.getSimpleName(),
                    "Exception at writing to output stream to config file", e);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                Log.d(ConfigUtil.class.getSimpleName(),
                        "Exception at writing config file", e);
            }
        }
        return false;
    }

    private static @Nullable JSONObject readObject() {
        File configFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + CONFIG_FILE);
        if (!configFile.isDirectory()) {
            InputStream in = null;
            try {
                in = new FileInputStream(configFile);
                return new JSONObject(IOUtil.readText(in, "UTF-8"));
            } catch (JSONException e) {
                Log.d(ConfigUtil.class.getSimpleName(),
                        "Exception at reading JSON parameters", e);
            } catch (IOException e) {
                Log.d(ConfigUtil.class.getSimpleName(),
                        "Exception at reading config file", e);
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    Log.d(ConfigUtil.class.getSimpleName(),
                            "Exception at reading config file", e);
                }
            }
        }
        return null;
    }

}
