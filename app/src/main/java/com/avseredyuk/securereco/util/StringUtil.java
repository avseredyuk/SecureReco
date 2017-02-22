package com.avseredyuk.securereco.util;

import com.avseredyuk.securereco.model.Call;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenfer on 2/15/17.
 */
public class StringUtil {
    public static String formatFileName(String callNumber, Date datetimeStarted) {
        return String.format("%s_%s%s",
                new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(datetimeStarted),
                callNumber,
                ".bin");
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }

    public static Call getCallFromFilename(String filename) {
        Call call = new Call();
        Pattern p = Pattern.compile("^(\\d{2})_(\\d{2})_(\\d{4})_(\\d{2})_(\\d{2})_(\\d{2})_(.+)\\.bin$");
        Matcher m = p.matcher(filename);

        if (m.matches() && (m.groupCount() == 7)) {
            int day = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            int hour = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            int second = Integer.parseInt(m.group(6));
            String number = m.group(7);

            Calendar c = Calendar.getInstance();
            c.set(year, month, day, hour, minute, second);
            Date datetimeStarted = c.getTime();

            call.setDatetimeStarted(datetimeStarted);
            call.setCallNumber(number);

            //todo
            call.setIsIncoming(true);

            return call;
        } else {
            //todo
            return null;
        }
    }

}
