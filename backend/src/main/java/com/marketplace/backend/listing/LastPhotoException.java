package com.marketplace.backend.listing;

public class LastPhotoException extends RuntimeException {

    public LastPhotoException() {
        super("A Listing must always have at least one photo");
    }
}
