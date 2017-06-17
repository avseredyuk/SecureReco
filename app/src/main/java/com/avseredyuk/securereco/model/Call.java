package com.avseredyuk.securereco.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class Call implements Parcelable {
    private String callNumber;
    private String contactName;
    private Date datetimeStarted;
    private Date dateTimeEnded;
    private boolean isIncoming;
    private String filename;

    public Call(String callNumber, Date datetimeStarted, boolean isIncoming) {
        this.callNumber = callNumber;
        this.datetimeStarted = datetimeStarted;
        this.isIncoming = isIncoming;
    }

    public static final Comparator<Call> CallDateComparator = new Comparator<Call>() {

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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getDateTimeEnded() {
        return dateTimeEnded;
    }

    public void setDateTimeEnded(Date dateTimeEnded) {
        this.dateTimeEnded = dateTimeEnded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Call(Parcel in) {
        callNumber = in.readString();
        contactName = in.readString();
        long tmpDatetimeStarted = in.readLong();
        datetimeStarted = tmpDatetimeStarted != -1 ? new Date(tmpDatetimeStarted) : null;
        long tmpDateTimeEnded = in.readLong();
        dateTimeEnded = tmpDateTimeEnded != -1 ? new Date(tmpDateTimeEnded) : null;
        isIncoming = in.readByte() != 0x00;
        filename = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(callNumber);
        dest.writeString(contactName);
        dest.writeLong(datetimeStarted != null ? datetimeStarted.getTime() : -1L);
        dest.writeLong(dateTimeEnded != null ? dateTimeEnded.getTime() : -1L);
        dest.writeByte((byte) (isIncoming ? 0x01 : 0x00));
        dest.writeString(filename);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Call> CREATOR = new Parcelable.Creator<Call>() {
        @Override
        public Call createFromParcel(Parcel in) {
            return new Call(in);
        }

        @Override
        public Call[] newArray(int size) {
            return new Call[size];
        }
    };
}
