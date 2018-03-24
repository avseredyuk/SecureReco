package com.avseredyuk.securereco.configuration;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import com.avseredyuk.securereco.dao.SQLiteConfigDao;
import com.avseredyuk.securereco.model.ConfigItem;
import com.avseredyuk.securereco.model.NotificationColor;
import com.avseredyuk.securereco.model.ResetAuthenticationStrategy;
import com.avseredyuk.securereco.util.AudioSourceEnum;

import java.util.HashMap;
import java.util.Map;

import static com.avseredyuk.securereco.util.Constant.APP_DIRECTORY;
import static com.avseredyuk.securereco.util.Constant.AUDIO_SOURCE;
import static com.avseredyuk.securereco.util.Constant.CALL_DIR;
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
    private static final Map<String, Object> DEFAULTS = new HashMap<>();
    static {
        DEFAULTS.put(NOTIFICATION_ON,
                Boolean.TRUE);
        DEFAULTS.put(AUDIO_SOURCE,
                AudioSourceEnum.VOICE_COMMUNICATION);
        DEFAULTS.put(NOTIFICATION_COLOR,
                NotificationColor.NIGHT);
        DEFAULTS.put(CALL_DIR,
                Environment.getExternalStorageDirectory() + "/" + APP_DIRECTORY + "/calls");
        DEFAULTS.put(RESET_AUTH_STRATEGY,
                ResetAuthenticationStrategy.WHEN_APP_GOES_TO_BACKGROUND);
        DEFAULTS.put(IS_ENABLED,
                Boolean.FALSE);
    }
    private final Context context;

    public Configuration(Context context) {
        this.context = context;
    }

    public boolean isConfigValid() {
        SQLiteConfigDao dao = new SQLiteConfigDao(context).open();
        int count = dao.findAll().size();
        dao.close();
        return count > 0;
    }

    public byte[] getPublicKey() {
        String value = getValue(PUBLIC_KEY);
        return value != null ? Base64.decode(value, Base64.DEFAULT) : (byte[]) DEFAULTS.get(PUBLIC_KEY);
    }

    public Configuration setPublicKey(byte[] publicKey) {
        setValue(PUBLIC_KEY, Base64.encodeToString(publicKey, Base64.DEFAULT));
        return this;
    }

    public byte[] getPrivateKeyEncoded() {
        String value = getValue(PRIVATE_KEY_ENCODED);
        return value != null ? Base64.decode(value, Base64.DEFAULT) : (byte[]) DEFAULTS.get(PRIVATE_KEY_ENCODED);
    }

    public Configuration setPrivateKeyEncoded(byte[] privateKeyEncoded) {
        setValue(PRIVATE_KEY_ENCODED, Base64.encodeToString(privateKeyEncoded, Base64.DEFAULT));
        return this;
    }

    public byte[] getPrivateKeyIV() {
        String value = getValue(PRIVATE_KEY_IV);
        return value != null ? Base64.decode(value, Base64.DEFAULT) : (byte[]) DEFAULTS.get(PRIVATE_KEY_IV);
    }

    public Configuration setPrivateKeyIV(byte[] privateKeyIV) {
        setValue(PRIVATE_KEY_IV, Base64.encodeToString(privateKeyIV, Base64.DEFAULT));
        return this;
    }

    public boolean isNotificationOn() {
        String value = getValue(NOTIFICATION_ON);
        return value != null ? Boolean.valueOf(value) : (Boolean) DEFAULTS.get(NOTIFICATION_ON);
    }

    public Configuration setNotificationOn(boolean notificationOn) {
        setValue(NOTIFICATION_ON, Boolean.toString(notificationOn));
        return this;
    }

    public AudioSourceEnum getAudioSource() {
        String value = getValue(AUDIO_SOURCE);
        return value != null ? AudioSourceEnum.valueOf(value) : (AudioSourceEnum) DEFAULTS.get(AUDIO_SOURCE);
    }

    public Configuration setAudioSource(AudioSourceEnum audioSource) {
        setValue(AUDIO_SOURCE, audioSource.toString());
        return this;
    }

    public String getCallDir() {
        String value = getValue(CALL_DIR);
        return value != null ? value : (String) DEFAULTS.get(CALL_DIR);
    }

    public Configuration setCallDir(String callDir) {
        setValue(CALL_DIR, callDir);
        return this;
    }

    public byte[] getPrivateKeyHMAC() {
        String value = getValue(PRIVATE_KEY_HMAC);
        return value != null ? Base64.decode(value, Base64.DEFAULT) : (byte[]) DEFAULTS.get(PRIVATE_KEY_HMAC);
    }

    public Configuration setPrivateKeyHMAC(byte[] privateKeyHMAC) {
        setValue(PRIVATE_KEY_HMAC, Base64.encodeToString(privateKeyHMAC, Base64.DEFAULT));
        return this;
    }

    public ResetAuthenticationStrategy getResetAuthenticationStrategy() {

        String value = getValue(RESET_AUTH_STRATEGY);
        return value != null ? ResetAuthenticationStrategy.valueOf(value) : (ResetAuthenticationStrategy) DEFAULTS.get(RESET_AUTH_STRATEGY);
    }

    public Configuration setResetAuthenticationStrategy(ResetAuthenticationStrategy resetAuthenticationStrategy) {
        setValue(RESET_AUTH_STRATEGY, resetAuthenticationStrategy.toString());
        return this;
    }

    public boolean isEnabled() {
        String value = getValue(IS_ENABLED);
        return value != null ? Boolean.valueOf(value) : (Boolean) DEFAULTS.get(IS_ENABLED);
    }

    public Configuration setEnabled(boolean enabled) {
        setValue(IS_ENABLED, Boolean.toString(enabled));
        return this;
    }

    public NotificationColor getNotificationColor() {
        String value = getValue(NOTIFICATION_COLOR);
        return value != null ? NotificationColor.valueOf(value) : (NotificationColor) DEFAULTS.get(NOTIFICATION_COLOR);
    }

    public Configuration setNotificationColor(NotificationColor notificationColor) {
        setValue(NOTIFICATION_COLOR, notificationColor.toString());
        return this;
    }

    private String getValue(String key) {
        SQLiteConfigDao dao = new SQLiteConfigDao(context).open();
        ConfigItem config = dao.findByKey(key);
        dao.close();
        return config.getValue();
    }

    private void setValue(String key, String value) {
        SQLiteConfigDao dao = new SQLiteConfigDao(context).open();
        if (dao.findByKey(key).getKey() == null) {
            dao.save(key, value);
        } else {
            dao.update(key, value);
        }
        dao.close();
    }
}
