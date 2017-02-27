package com.avseredyuk.securereco.exception;

/**
 * Created by lenfer on 2/27/17.
 */
public class ParserException extends Exception {
    public ParserException() {
        super();
    }

    public ParserException(String detailMessage) {
        super(detailMessage);
    }

    public ParserException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParserException(Throwable throwable) {
        super(throwable);
    }
}
