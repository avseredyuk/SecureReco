package com.avseredyuk.securereco.exception;

/**
 * Created by lenfer on 3/5/17.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String detailMessage) {
        super(detailMessage);
    }
}
