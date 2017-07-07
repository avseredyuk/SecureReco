package com.avseredyuk.securereco.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.avseredyuk.securereco.util.ContactResolverUtil;

import java.util.Date;

/**
 * Created by lenfer on 2/15/17.
 */
public class Call implements Parcelable {
    private String callNumber;
    private String contactName;
    private Date dateTimeStarted;
    private Date dateTimeEnded;
    private boolean isIncoming;
    private String filename;
    private boolean checked;
    private Bitmap photo;
    private long id;
    private boolean starred;
    private String notes;

    public Call() {
    }

    public Call(String callNumber, Date dateTimeStarted, boolean isIncoming) {
        this.callNumber = callNumber;
        this.dateTimeStarted = dateTimeStarted;
        this.isIncoming = isIncoming;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getContactName() {
        if (contactName == null) {
            contactName = ContactResolverUtil.getContactName(this);
        }
        return contactName;
    }

    public Date getDateTimeStarted() {
        return dateTimeStarted;
    }

    public void setDateTimeStarted(Date dateTimeStarted) {
        this.dateTimeStarted = dateTimeStarted;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Bitmap getPhoto() {
        if (photo == null) {
            photo = ContactResolverUtil.retrieveContactPhotoCircleCropped(this);
        }
        return photo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Call(Parcel in) {
        callNumber = in.readString();
        contactName = in.readString();
        long tmpDatetimeStarted = in.readLong();
        dateTimeStarted = tmpDatetimeStarted != -1 ? new Date(tmpDatetimeStarted) : null;
        long tmpDateTimeEnded = in.readLong();
        dateTimeEnded = tmpDateTimeEnded != -1 ? new Date(tmpDateTimeEnded) : null;
        isIncoming = in.readByte() != 0x00;
        filename = in.readString();
        checked = in.readByte() != 0x00;
        id = in.readLong();
        starred = in.readByte() != 0x00;
        notes = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(callNumber);
        dest.writeString(contactName);
        dest.writeLong(dateTimeStarted != null ? dateTimeStarted.getTime() : -1L);
        dest.writeLong(dateTimeEnded != null ? dateTimeEnded.getTime() : -1L);
        dest.writeByte((byte) (isIncoming ? 0x01 : 0x00));
        dest.writeString(filename);
        dest.writeByte((byte) (checked ? 0x01 : 0x00));
        dest.writeLong(id);
        dest.writeByte((byte) (starred ? 0x01 : 0x00));
        dest.writeString(notes);
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
