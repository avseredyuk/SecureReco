package com.avseredyuk.securereco.model;

import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class Call {
    private String callNumber;
    private Date datetimeStarted;
    private Date datetimeEnded;
    private boolean isIncoming;

    public Call(String callNumber, Date datetimeStarted, Date datetimeEnded, boolean isIncoming) {
        this.callNumber = callNumber;
        this.datetimeStarted = datetimeStarted;
        this.datetimeEnded = datetimeEnded;
        this.isIncoming = isIncoming;
    }

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

    public Date getDatetimeEnded() {
        return datetimeEnded;
    }

    public void setDatetimeEnded(Date datetimeEnded) {
        this.datetimeEnded = datetimeEnded;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIsIncoming(boolean isIncoming) {
        this.isIncoming = isIncoming;
    }
}
