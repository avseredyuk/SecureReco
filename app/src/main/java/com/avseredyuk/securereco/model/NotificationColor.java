package com.avseredyuk.securereco.model;

/**
 * Created by lenfer on 8/12/17.
 */

public enum NotificationColor {
    DAY(0), NIGHT(1);

    private int value;

    NotificationColor(int value) {
        this.value = value;
    }
}
