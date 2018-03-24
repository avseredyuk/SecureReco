package com.avseredyuk.securereco.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.avseredyuk.securereco.model.ConfigItem;
import com.avseredyuk.securereco.sql.DatabaseHelper;

import java.util.HashSet;
import java.util.Set;

import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_CONFIG_KEY;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_CONFIG_VALUE;
import static com.avseredyuk.securereco.sql.DatabaseHelper.TABLE_CONFIG;

/**
 * Created by lenfer on 3/18/18.
 */

public class SQLiteConfigDao {
    private static final String[] allColumns = {COLUMN_CONFIG_KEY, COLUMN_CONFIG_VALUE};
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public SQLiteConfigDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public SQLiteConfigDao open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public Set<ConfigItem> findAll() {
        Set<ConfigItem> configs = new HashSet<>();
        Cursor cursor = db.query(TABLE_CONFIG, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ConfigItem config = cursorToConfig(cursor);
            configs.add(config);
            cursor.moveToNext();
        }
        cursor.close();
        return configs;
    }

    public ConfigItem findByKey(String key) {
        Cursor cursor = db.query(TABLE_CONFIG,
                allColumns, COLUMN_CONFIG_KEY + " = ?", new String[]{ key },
                null, null, null);
        cursor.moveToFirst();
        ConfigItem config = cursorToConfig(cursor);
        cursor.close();
        return config;
    }

    public void update(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CONFIG_VALUE, value);
        db.update(TABLE_CONFIG, cv, COLUMN_CONFIG_KEY + " = ?", new String[] { key });
    }

    public void save(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONFIG_KEY, key);
        values.put(COLUMN_CONFIG_VALUE, value);
        long insertId = db.insert(TABLE_CONFIG, null, values);
        if (insertId == -1) {
            Log.w(SQLiteConfigDao.class.getName(), String.format("Error saving config KEY = %s, VALUE = %s", key, value));
        }
    }

    private ConfigItem cursorToConfig(Cursor cursor) {
        ConfigItem config = new ConfigItem();
        if (!cursor.isAfterLast()) {
            config.setKey(cursor.getString(0));
            config.setValue(cursor.getString(1));
        }
        return config;
    }
}
