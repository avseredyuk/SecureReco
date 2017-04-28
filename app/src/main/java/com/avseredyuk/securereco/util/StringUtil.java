package com.avseredyuk.securereco.util;

import android.os.Environment;

import com.avseredyuk.securereco.exception.ParserException;
import com.avseredyuk.securereco.model.Call;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        call.setFilename(Environment.getExternalStorageDirectory() + "/" + CALL_LOGS_DIRECTORY + "/" + filename);
        Pattern p = Pattern.compile("^(\\d{2})_(\\d{2})_(\\d{4})_(\\d{2})_(\\d{2})_(\\d{2})_(.+)_([IO])\\.bin$");
        Matcher m = p.matcher(filename);

        if (m.matches() && (m.groupCount() == 8)) {

            int day = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            int hour = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            int second = Integer.parseInt(m.group(6));
            String number = m.group(7);
            String isIncoming = m.group(8);

            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day, hour, minute, second);
            Date datetimeStarted = c.getTime();

            call.setDatetimeStarted(datetimeStarted);
            call.setCallNumber(number);
            call.setIsIncoming("I".equals(isIncoming));

            return call;
        } else {
            throw new ParserException("Exception at parsing encrypted call filename");
        }
    }

}
