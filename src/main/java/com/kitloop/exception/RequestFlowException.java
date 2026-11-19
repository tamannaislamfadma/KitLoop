package com.kitloop.exception;

public class RequestFlowException
        extends RuntimeException {

    public RequestFlowException(
            String message
    ) {
        super(message);
    }
}