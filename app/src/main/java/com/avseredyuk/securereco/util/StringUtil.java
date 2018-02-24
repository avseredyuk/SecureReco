package com.avseredyuk.securereco.util;

import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class StringUtil {
    private static final ThreadLocal<SimpleDateFormat> simpleDateFormatDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> simpleDateFormatHeader = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEE, d MMM");
        }
    };

    private StringUtil() {
    }

    public static String addOrChangeFileExtension(String filePath, String extension) {
        int index = filePath.lastIndexOf('.');
        if (index == -1) {
            return filePath + extension;
        } else {
            return filePath.substring(0, index) + extension;
        }
    }

    public static boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatTimeInterval(Date d1, Date d2) {
        return formatTimeInterval(d1.getTime(), d2.getTime());
    }

    public static String formatTimeInterval(long l1, long l2) {
        long s = (l2 - l1) / 1000;
        long sec = s % 60;
        long min = (s % 3600) / 60;
        long hour = s / 3600;
        if (hour > 0) {
            return String.format("%d:%02d:%02d", hour, min, sec);
        } else {
            return String.format("%02d:%02d", min, sec);
        }
    }

    public static String formatDate(Date date) {
        return simpleDateFormatDate.get().format(date);
    }

    public static String formatDateOnly(Date date) {
        return simpleDateFormatHeader.get().format(date);
    }

    public static boolean isEditTextDataValid(EditText e1, EditText e2) {
        final String s1 = e1.getText().toString();
        final String s2 = e2.getText().toString();
        return (s1.length() != 0 && s2.length() != 0 && s1.equals(s2));
    }

}
