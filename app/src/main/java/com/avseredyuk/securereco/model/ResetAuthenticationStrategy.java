package com.avseredyuk.securereco.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton_Serediuk on 6/7/2017.
 */

public enum ResetAuthenticationStrategy {
    NO_RESET_KEEP_AUTHENTICATED(0),
    WHEN_APP_GOES_TO_BACKGROUND(1),
    ON_TIMEOUT_OF_INACTIVITY(2);

    private int value;
    private static Map<Integer, ResetAuthenticationStrategy> map = new HashMap<>();

    static {
        for (ResetAuthenticationStrategy strategyEnum : ResetAuthenticationStrategy.values()) {
            map.put(strategyEnum.value, strategyEnum);
        }
    }

    ResetAuthenticationStrategy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ResetAuthenticationStrategy valueOf(int value) {
        return map.get(value);
    }
}
