package com.marketplace.backend.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("No user found with username " + username);
    }
}
