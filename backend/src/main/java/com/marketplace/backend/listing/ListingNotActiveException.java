package com.marketplace.backend.listing;

public class ListingNotActiveException extends RuntimeException {

    public ListingNotActiveException() {
        super("Only an ACTIVE listing can be deleted");
    }
}
