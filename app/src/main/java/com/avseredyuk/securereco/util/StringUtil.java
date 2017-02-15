package com.avseredyuk.securereco.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class StringUtil {
    public static String formatFileName(String callNumber, Date datetimeStarted) {
        return String.format("%s_%s%s",
                new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(datetimeStarted),
                callNumber,
                ".amr");
    }
}
