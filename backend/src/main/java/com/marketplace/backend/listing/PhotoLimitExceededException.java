package com.marketplace.backend.listing;

public class PhotoLimitExceededException extends RuntimeException {

    public PhotoLimitExceededException() {
        super("A Listing cannot have more than 6 photos");
    }
}
