package com.marketplace.backend.auth;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String username) {
        super("Username '%s' is already taken".formatted(username));
    }
}
