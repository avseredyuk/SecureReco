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
            return new SimpleDateFormat("dd/MM/yyyy HH:mm");
        }
    };

    private StringUtil() {
    }

    public static String getCallLogsDir() {
        return Environment.getExternalStorageDirectory() + "/" + CALL_LOGS_DIRECTORY;
    }

    public static String formatFileName(String callNumber, Date datetimeStarted, boolean isIncoming) {
        return String.format("%s_%s_%s%s",
                simpleDateFormatFileName.get().format(datetimeStarted),
                callNumber,
                isIncoming ? "I" : "O",
                ".bin");
    }

    public static String formatDate(Date date) {
        return simpleDateFormatDate.get().format(date);
    }

    public static Call getCallFromFilename(String filename) throws ParserException{
        Call call = new Call();
        call.setFilename(StringUtil.getCallLogsDir() + "/" + filename);

        try {
            int day = Integer.parseInt(filename.substring(0, 2));
            int month = Integer.parseInt(filename.substring(3, 5));
            int year = Integer.parseInt(filename.substring(6, 10));
            int hour = Integer.parseInt(filename.substring(11, 13));
            int minute = Integer.parseInt(filename.substring(14, 16));
            int second = Integer.parseInt(filename.substring(17, 19));
            String number = filename.substring(20, filename.length() - 6);
            boolean isIncoming = "I".equals(filename.substring(filename.length()-5, filename.length()-4));

            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day, hour, minute, second);
            Date datetimeStarted = c.getTime();

            call.setDatetimeStarted(datetimeStarted);
            call.setCallNumber(number);
            call.setIsIncoming(isIncoming);

            return call;

        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            throw new ParserException("Exception at parsing encrypted call filename", e);
        }
    }

}
