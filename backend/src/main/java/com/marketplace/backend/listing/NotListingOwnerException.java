package com.marketplace.backend.listing;

public class NotListingOwnerException extends RuntimeException {

    public NotListingOwnerException() {
        super("You do not own this listing");
    }
}
