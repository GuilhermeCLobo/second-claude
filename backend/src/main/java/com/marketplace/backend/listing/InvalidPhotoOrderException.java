package com.marketplace.backend.listing;

public class InvalidPhotoOrderException extends RuntimeException {

    public InvalidPhotoOrderException() {
        super("The given photo ids must match exactly this listing's current photos");
    }
}
