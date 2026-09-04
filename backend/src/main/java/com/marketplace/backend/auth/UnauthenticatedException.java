package com.marketplace.backend.auth;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("Authentication required");
    }
}
