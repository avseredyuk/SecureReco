package com.avseredyuk.securereco.util;

import android.os.Environment;

import com.avseredyuk.securereco.exception.ParserException;
import com.avseredyuk.securereco.model.Call;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.avseredyuk.securereco.util.Constant.CALL_LOGS_DIRECTORY;

/**
 * Created by lenfer on 2/15/17.
 */
public class StringUtil {
    private static ThreadLocal<SimpleDateFormat> simpleDateFormatFileName = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        }
    };
    private static ThreadLocal<SimpleDateFormat> simpleDateFormatDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    private static ThreadLocal<SimpleDateFormat> simpleDateFormatHeader = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEE, d MMM");
        }
    };

    private StringUtil() {
    }

    public static String getCallLogsDir() {
        return Environment.getExternalStorageDirectory() + "/" + CALL_LOGS_DIRECTORY;
    }

    public static String formatFileName(Call call, boolean isTemporary) {
        return String.format("%s%s_%s_%s%s",
                simpleDateFormatFileName.get().format(call.getDatetimeStarted()),
                isTemporary ? "" : "_" + simpleDateFormatFileName.get().format(call.getDateTimeEnded()),
                call.getCallNumber(),
                call.isIncoming() ? "I" : "O",
                isTemporary ? ".tmp" : ".bin");
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
        long s = (d2.getTime() - d1.getTime()) / 1000;
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

    public static Call getCallFromFilename(String filename) throws ParserException{
        try {
            int day = Integer.parseInt(filename.substring(0, 2));
            int month = Integer.parseInt(filename.substring(3, 5));
            int year = Integer.parseInt(filename.substring(6, 10));
            int hour = Integer.parseInt(filename.substring(11, 13));
            int minute = Integer.parseInt(filename.substring(14, 16));
            int second = Integer.parseInt(filename.substring(17, 19));
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day, hour, minute, second);
            Date datetimeStarted = c.getTime();

            day = Integer.parseInt(filename.substring(20, 22));
            month = Integer.parseInt(filename.substring(23, 25));
            year = Integer.parseInt(filename.substring(26, 30));
            hour = Integer.parseInt(filename.substring(31, 33));
            minute = Integer.parseInt(filename.substring(34, 36));
            second = Integer.parseInt(filename.substring(37, 39));
            c = Calendar.getInstance();
            c.set(year, month - 1, day, hour, minute, second);
            Date dateTimeEnded = c.getTime();

            String number = filename.substring(40, filename.length() - 6);
            boolean isIncoming = "I".equals(filename.substring(filename.length()-5, filename.length()-4));

            Call call = new Call(number, datetimeStarted, isIncoming);
            call.setDateTimeEnded(dateTimeEnded);
            call.setFilename(StringUtil.getCallLogsDir() + "/" + filename);

            return call;

        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            throw new ParserException("Exception at parsing encrypted call filename", e);
        }
    }

}
