package com.marketplace.backend.listing;

public class ListingNotActiveException extends RuntimeException {

    public ListingNotActiveException() {
        super("This listing is not ACTIVE");
    }
}
