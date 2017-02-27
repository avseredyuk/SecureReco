package com.avseredyuk.securereco.exception;

/**
 * Created by lenfer on 2/27/17.
 */
public class CryptoException extends Exception {
    public CryptoException() {
        super();
    }

    public CryptoException(String detailMessage) {
        super(detailMessage);
    }

    public CryptoException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CryptoException(Throwable throwable) {
        super(throwable);
    }
}
