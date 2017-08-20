package com.avseredyuk.securereco.configuration;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.avseredyuk.securereco.model.NotificationColor;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.util.AudioSourceEnum;
import com.avseredyuk.securereco.util.IOUtil;

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
import static com.avseredyuk.securereco.util.Constant.AUDIO_SOURCE;
import static com.avseredyuk.securereco.util.Constant.CALL_DIR;
import static com.avseredyuk.securereco.util.Constant.CONFIG_FILE;
import static com.avseredyuk.securereco.util.Constant.IS_ENABLED;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_COLOR;
import static com.avseredyuk.securereco.util.Constant.NOTIFICATION_ON;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_ENCODED;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_HMAC;
import static com.avseredyuk.securereco.util.Constant.PRIVATE_KEY_IV;
import static com.avseredyuk.securereco.util.Constant.PUBLIC_KEY;
import static com.avseredyuk.securereco.util.Constant.RESET_AUTH_STRATEGY;

/**
 * Created by Anton_Serediuk on 7/6/2017.
 */

public class Configuration {
    private JSONObject o;
    private byte[] publicKey;
    private byte[] privateKeyEncoded;
    private byte[] privateKeyIV;
    private Boolean notificationOn;
    private AudioSourceEnum audioSource;
    private String callDir;
    private byte[] privateKeyHMAC;
    private ResetAuthenticationStrategy resetAuthenticationStrategy;
    private NotificationColor notificationColor;
    private Boolean isEnabled;

    public Configuration() {
        o = readObject();
    }

    public void commit() {
        File configFile = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY + "/" + CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        OutputStream out = null;
        try {
            out = new FileOutputStream(configFile);

            if (o == null) {
                o = new JSONObject();
            }

            o.put(PUBLIC_KEY,
                    publicKey == null
                            ? getValue(PUBLIC_KEY)
                            : Base64.encodeToString(publicKey, Base64.DEFAULT));

            o.put(PRIVATE_KEY_ENCODED,
                    privateKeyEncoded == null
                            ? getValue(PRIVATE_KEY_ENCODED)
                            : Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));

            o.put(PRIVATE_KEY_IV,
                    privateKeyIV == null
                            ? getValue(PRIVATE_KEY_IV)
                            : Base64.encodeToString(privateKeyIV, Base64.DEFAULT));

            o.put(NOTIFICATION_ON,
                    notificationOn == null
                            ? valueOrDefault(getValue(NOTIFICATION_ON), true)
                            : notificationOn);

            o.put(AUDIO_SOURCE,
                    audioSource == null
                            ? valueOrDefault(getValue(AUDIO_SOURCE), AudioSourceEnum.VOICE_COMMUNICATION)
                            : audioSource.toString());

            o.put(NOTIFICATION_COLOR,
                    notificationColor == null
                            ? valueOrDefault(getValue(NOTIFICATION_COLOR), NotificationColor.NIGHT)
                            : notificationColor.toString());

            o.put(CALL_DIR,
                    callDir == null
                            ? valueOrDefault(getValue(CALL_DIR), Environment.getExternalStorageDirectory() + "/" + APP_DIRECTORY + "/calls")
                            : callDir);

            o.put(PRIVATE_KEY_HMAC,
                    privateKeyHMAC == null
                            ? getValue(PRIVATE_KEY_HMAC)
                            : Base64.encodeToString(privateKeyHMAC, Base64.DEFAULT));

            o.put(RESET_AUTH_STRATEGY,
                    resetAuthenticationStrategy == null
                            ? valueOrDefault(getValue(RESET_AUTH_STRATEGY), ResetAuthenticationStrategy.WHEN_APP_GOES_TO_BACKGROUND.getValue())
                            : resetAuthenticationStrategy.getValue());

            o.put(IS_ENABLED,
                    isEnabled == null
                            ? valueOrDefault(getValue(IS_ENABLED), true)
                            : isEnabled);

            out.write(o.toString().getBytes(Charset.forName("UTF-8")));

        } catch (JSONException e) {
            Log.d(Configuration.class.getSimpleName(),
                    "Exception at saving JSON parameters", e);
        } catch (IOException e) {
            Log.d(Configuration.class.getSimpleName(),
                    "Exception at writing to output stream to config file", e);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                Log.d(Configuration.class.getSimpleName(),
                        "Exception at writing config file", e);
            }
        }
    }

    private Object valueOrDefault(Object value, Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    public boolean isConfigValid() {
        return o != null;
    }

    public byte[] getPublicKey() {
        if (publicKey == null) {
            publicKey = Base64.decode(getValue(PUBLIC_KEY), Base64.DEFAULT);
        }
        return publicKey;
    }

    public Configuration setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public byte[] getPrivateKeyEncoded() {
        if (privateKeyEncoded == null) {
            privateKeyEncoded = Base64.decode(getValue(PRIVATE_KEY_ENCODED), Base64.DEFAULT);
        }
        return privateKeyEncoded;
    }

    public Configuration setPrivateKeyEncoded(byte[] privateKeyEncoded) {
        this.privateKeyEncoded = privateKeyEncoded;
        return this;
    }

    public byte[] getPrivateKeyIV() {
        if (privateKeyIV == null) {
            privateKeyIV = Base64.decode(getValue(PRIVATE_KEY_IV), Base64.DEFAULT);
        }
        return privateKeyIV;
    }

    public Configuration setPrivateKeyIV(byte[] privateKeyIV) {
        this.privateKeyIV = privateKeyIV;
        return this;
    }

    public boolean isNotificationOn() {
        if (notificationOn == null) {
            notificationOn = Boolean.valueOf(getValue(NOTIFICATION_ON));
        }
        return notificationOn;
    }

    public Configuration setNotificationOn(boolean notificationOn) {
        this.notificationOn = notificationOn;
        return this;
    }

    public AudioSourceEnum getAudioSource() {
        if (audioSource == null) {
            audioSource = AudioSourceEnum.valueOf(getValue(AUDIO_SOURCE));
        }
        return audioSource;
    }

    public Configuration setAudioSource(AudioSourceEnum audioSource) {
        this.audioSource = audioSource;
        return this;
    }

    public String getCallDir() {
        if (callDir == null) {
            callDir = getValue(CALL_DIR);
        }
        return callDir;
    }

    public Configuration setCallDir(String callDir) {
        this.callDir = callDir;
        return this;
    }

    public byte[] getPrivateKeyHMAC() {
        if (privateKeyHMAC == null) {
            privateKeyHMAC = Base64.decode(getValue(PRIVATE_KEY_HMAC), Base64.DEFAULT);
        }
        return privateKeyHMAC;
    }

    public Configuration setPrivateKeyHMAC(byte[] privateKeyHMAC) {
        this.privateKeyHMAC = privateKeyHMAC;
        return this;
    }

    public ResetAuthenticationStrategy getResetAuthenticationStrategy() {
        if (resetAuthenticationStrategy == null) {
            resetAuthenticationStrategy = ResetAuthenticationStrategy.valueOf(Integer.parseInt(getValue(RESET_AUTH_STRATEGY)));
        }
        return resetAuthenticationStrategy;
    }

    public Configuration setResetAuthenticationStrategy(ResetAuthenticationStrategy resetAuthenticationStrategy) {
        this.resetAuthenticationStrategy = resetAuthenticationStrategy;
        return this;
    }

    public boolean isEnabled() {
        if (isEnabled == null) {
            isEnabled = Boolean.valueOf(getValue(IS_ENABLED));
        }
        return isEnabled;
    }

    public Configuration setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public NotificationColor getNotificationColor() {
        if (notificationColor == null) {
            notificationColor = NotificationColor.valueOf(getValue(NOTIFICATION_COLOR));
        }
        return notificationColor;
    }

    public Configuration setNotificationColor(NotificationColor notificationColor) {
        this.notificationColor = notificationColor;
        return this;
    }

    private @Nullable JSONObject readObject() {
        File configFile = new File(Environment.getExternalStorageDirectory(), "/" + APP_DIRECTORY + "/" + CONFIG_FILE);
        if (!configFile.isDirectory()) {
            InputStream in = null;
            try {
                in = new FileInputStream(configFile);
                return new JSONObject(IOUtil.readText(in, "UTF-8"));
            } catch (JSONException e) {
                Log.d(Configuration.class.getSimpleName(),
                        "Exception at reading JSON parameters", e);
            } catch (IOException e) {
                Log.d(Configuration.class.getSimpleName(),
                        "Exception at reading config file", e);
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    Log.d(Configuration.class.getSimpleName(),
                            "Exception at reading config file", e);
                }
            }
        }
        return null;
    }

    private String getValue(String key) {
        if (o != null) {
            try {
                return o.getString(key);
            } catch (JSONException e) {
                //todo
                e.printStackTrace();
            }
        }
        return null;
    }
}
