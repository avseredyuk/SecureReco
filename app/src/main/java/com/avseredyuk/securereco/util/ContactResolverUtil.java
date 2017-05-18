package com.avseredyuk.securereco.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.avseredyuk.securereco.application.Application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by lenfer on 2/23/17.
 */
public class ContactResolverUtil {

    private ContactResolverUtil() {
    }

    //todo: refactor this trash
    public static String getContactName(Context context, String number) {
        Application application = (Application) context.getApplicationContext();
        Map<String, String> contactNameCache = application.getContactNameCache();
        String contactName = contactNameCache.get(number);
        if (contactName != null) {
            return contactName;
        }
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            contactNameCache.put(number, number);
            return number;
        }
        contactName = number;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if(!cursor.isClosed()) {
            cursor.close();
        }
        contactNameCache.put(number, contactName);
        return contactName;
    }

    //todo: refactor this trash
    public static Bitmap retrieveContactPhoto(Context context, String number) {
        Application application = (Application) context.getApplicationContext();
        Map<String, Bitmap> contactPhotoCache = application.getContactPhotoCache();
        Bitmap photo = contactPhotoCache.get(number);
        if (photo != null) {
            return photo;
        }
        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
        Cursor cursor =
                contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            cursor.close();
        }
        photo = contactPhotoCache.get(null);
        try {
            if (contactId != null) {
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));
                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            Log.e(ContactResolverUtil.class.getSimpleName(),
                    "Error retrieving contact photo", e);
        }
        contactPhotoCache.put(number, photo);
        return photo;
    }
}
