package com.avseredyuk.securereco.exception;

/**
 * Created by lenfer on 3/5/17.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String detailMessage) {
        super(detailMessage);
    }

    public AuthenticationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AuthenticationException(Throwable throwable) {
        super(throwable);
    }
}
