package com.kitloop.exception;

public class AuthenticationFlowException extends RuntimeException {

    public AuthenticationFlowException(String message) {
        super(message);
    }
}