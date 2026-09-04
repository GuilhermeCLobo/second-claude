package com.marketplace.backend.photo;

public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(String reference) {
        super("No photo found for reference " + reference);
    }
}
