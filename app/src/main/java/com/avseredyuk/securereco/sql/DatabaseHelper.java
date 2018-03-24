package com.avseredyuk.securereco.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Anton_Serediuk on 7/4/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_CALLS = "calls";
    public static final String COLUMN_CALLS_ID = "_id";
    public static final String COLUMN_CALLS_CALL_NUMBER = "call_number";
    public static final String COLUMN_CALLS_DATETIME_STARTED = "date_time_started";
    public static final String COLUMN_CALLS_DATETIME_ENDED = "date_time_ended";
    public static final String COLUMN_CALLS_IS_INCOMING = "is_incoming";
    public static final String COLUMN_CALLS_FILENAME = "filename";
    public static final String COLUMN_CALLS_STARRED = "is_starred";
    public static final String COLUMN_CALLS_NOTES = "notes";
    public static final String TABLE_CONFIG = "config";
    public static final String COLUMN_CONFIG_KEY = "key";
    public static final String COLUMN_CONFIG_VALUE = "value";

    private static final String DATABASE_NAME = "calls.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_CALLS_TABLE = "CREATE TABLE " + TABLE_CALLS + "( " +
            COLUMN_CALLS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CALLS_CALL_NUMBER + " TEXT NOT NULL, " +
            COLUMN_CALLS_DATETIME_STARTED + " INTEGER NOT NULL, " +
            COLUMN_CALLS_DATETIME_ENDED + " INTEGER, " +
            COLUMN_CALLS_IS_INCOMING + " INTEGER NOT NULL, " +
            COLUMN_CALLS_FILENAME + " TEXT NOT NULL, " +
            COLUMN_CALLS_STARRED + " INTEGER, " +
            COLUMN_CALLS_NOTES + " TEXT" +
            ");";

    private static final String CREATE_CONFIG_TABLE = "CREATE TABLE " + TABLE_CONFIG + "( " +
            COLUMN_CONFIG_KEY + " TEXT PRIMARY KEY, " +
            COLUMN_CONFIG_VALUE + " TEXT NOT NULL" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CALLS_TABLE);
        db.execSQL(CREATE_CONFIG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIG);
        onCreate(db);
    }
}
