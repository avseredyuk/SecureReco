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
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CALL_NUMBER = "call_number";
    public static final String COLUMN_DATETIME_STARTED = "date_time_started";
    public static final String COLUMN_DATETIME_ENDED = "date_time_ended";
    public static final String COLUMN_IS_INCOMING = "is_incoming";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_STARRED = "is_starred";
    public static final String COLUMN_NOTES = "notes";

    private static final String DATABASE_NAME = "calls.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_CALLS + "( " +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CALL_NUMBER + " TEXT NOT NULL, " +
            COLUMN_DATETIME_STARTED + " INTEGER NOT NULL, " +
            COLUMN_DATETIME_ENDED + " INTEGER, " +
            COLUMN_IS_INCOMING + " INTEGER NOT NULL, " +
            COLUMN_FILENAME + " TEXT NOT NULL, " +
            COLUMN_STARRED + " INTEGER, " +
            COLUMN_NOTES + " TEXT" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLS);
        onCreate(db);
    }
}
