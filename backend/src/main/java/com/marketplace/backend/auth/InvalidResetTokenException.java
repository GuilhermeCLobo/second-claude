package com.marketplace.backend.auth;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Reset token is invalid or expired");
    }
}
