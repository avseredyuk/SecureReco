package com.avseredyuk.securereco.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.avseredyuk.securereco.model.Call;
import com.avseredyuk.securereco.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_CALL_NUMBER;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_DATETIME_ENDED;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_DATETIME_STARTED;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_FILENAME;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_ID;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_IS_INCOMING;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_NOTES;
import static com.avseredyuk.securereco.sql.DatabaseHelper.COLUMN_STARRED;
import static com.avseredyuk.securereco.sql.DatabaseHelper.TABLE_CALLS;

/**
 * Created by Anton_Serediuk on 7/4/2017.
 */

public class SQLiteCallDao {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private String[] allColumns = { COLUMN_ID, COLUMN_CALL_NUMBER, COLUMN_DATETIME_STARTED,
            COLUMN_DATETIME_ENDED, COLUMN_IS_INCOMING, COLUMN_FILENAME, COLUMN_STARRED, COLUMN_NOTES};

    public SQLiteCallDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public SQLiteCallDao open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public List<Call> findAll() {
        List<Call> calls = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CALLS, allColumns,
                DatabaseHelper.COLUMN_DATETIME_ENDED + " IS NOT NULL", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Call call = cursorToCall(cursor);
            calls.add(call);
            cursor.moveToNext();
        }
        cursor.close();
        return calls;
    }

    public List<Call> findAllOrderedByDate() {
        List<Call> calls = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CALLS, allColumns,
                DatabaseHelper.COLUMN_DATETIME_ENDED + " IS NOT NULL", null, null, null,
                COLUMN_DATETIME_STARTED + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Call call = cursorToCall(cursor);
            calls.add(call);
            cursor.moveToNext();
        }
        cursor.close();
        return calls;
    }

    public boolean delete(Call call) {
        return db.delete(TABLE_CALLS, COLUMN_ID + " = " + call.getId(), null) == 1;
    }

    public Call persistCall(Call call) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CALL_NUMBER, call.getCallNumber());
        values.put(DatabaseHelper.COLUMN_IS_INCOMING, call.isIncoming());
        values.put(DatabaseHelper.COLUMN_DATETIME_STARTED, call.getDateTimeStarted().getTime());
        values.put(DatabaseHelper.COLUMN_FILENAME, call.getFilename());
        long insertId = db.insert(DatabaseHelper.TABLE_CALLS, null,
                values);
        Cursor cursor = db.query(DatabaseHelper.TABLE_CALLS,
                allColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Call newCall = cursorToCall(cursor);
        cursor.close();
        return newCall;
    }

    public void updateCallDateEnded(Call call) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_DATETIME_ENDED, call.getDateTimeEnded().getTime());
        db.update(DatabaseHelper.TABLE_CALLS, cv, DatabaseHelper.COLUMN_ID + " = " + call.getId(), null);
    }

    private Call cursorToCall(Cursor cursor) {
        Call call = new Call();
        call.setId(cursor.getLong(0));
        call.setCallNumber(cursor.getString(1));
        call.setDateTimeStarted(new Date(cursor.getLong(2)));
        call.setDateTimeEnded(new Date(cursor.getLong(3)));
        call.setIsIncoming(cursor.getInt(4) != 0);
        call.setFilename(cursor.getString(5));
        call.setStarred(cursor.getInt(6) != 0);
        call.setNotes(cursor.getString(7));
        return call;
    }
}
