package com.marketplace.backend.listing;

public class MissingPhotoException extends RuntimeException {

    public MissingPhotoException() {
        super("A Listing requires exactly one photo");
    }
}
