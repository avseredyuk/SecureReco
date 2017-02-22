package com.avseredyuk.securereco.model;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class Call {
    private String callNumber;
    private Date datetimeStarted;
    private boolean isIncoming;

    public Call() {
    }

    public Call(String callNumber, Date datetimeStarted, boolean isIncoming) {
        this.callNumber = callNumber;
        this.datetimeStarted = datetimeStarted;
        this.isIncoming = isIncoming;
    }

    public static Comparator<Call> CallDateComparator = new Comparator<Call>() {

        public int compare(Call call1, Call call2) {
            return call2.getDatetimeStarted().compareTo(call1.getDatetimeStarted());
        }

    };

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public Date getDatetimeStarted() {
        return datetimeStarted;
    }

    public void setDatetimeStarted(Date datetimeStarted) {
        this.datetimeStarted = datetimeStarted;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIsIncoming(boolean isIncoming) {
        this.isIncoming = isIncoming;
    }
}
