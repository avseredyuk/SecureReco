package com.avseredyuk.securereco.util;

/**
 * Created by lenfer on 2/15/17.
 */
public class Constant {
    public static final String APP_DIRECTORY = "SecureRecoApp";
    public static final String CONFIG_FILE = "configuration.json";
    public static final String PRIVATE_KEY_HMAC = "privateKeyHMAC";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY_ENCODED = "privateKeyEncoded";
    public static final String PRIVATE_KEY_IV = "privateKeyIV";
    public static final String IS_ENABLED = "isEnabled";
    public static final String NOTIFICATION_ON = "notificationOn";
    public static final String RESET_AUTH_STRATEGY = "resetAuthStrategy";
    public static final String CALL_DIR = "callDir";
    public static final int BUF_SIZE = 8192;
    public static final int NOTIFICATION_NEW_RECORD_ID = 61616;
    public static final int SPLASH_SHOW_TIME_IN_SECONDS = 2;
    public static final String BWIS_ACTION = "action";
    public static final String BWIS_DESTINATION_REGENERATE_KEYS = "regenerate_keys";
    public static final String BWIS_DESTINATION_CHANGE_FOLDER = "change_folder";
    public static final String OLD_PRIVATE_KEY_INTENT_EXTRA_NAME = "oldPrivateKey";
    public static final String OLD_FOLDER_PATH = "oldFolderPath";
    public static final String NEW_FOLDER_PATH = "newFolderPath";
    public static final String INTENT_BROADCAST_RESET_AUTH = "com.avseredyuk.securereco.RESET_AUTH";
    public static final int RESET_AUTH_DELAY = 1000 * 60;
}
