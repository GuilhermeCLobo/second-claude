package com.marketplace.backend.listing;

public class CannotBuyOwnListingException extends RuntimeException {

    public CannotBuyOwnListingException() {
        super("You cannot buy your own listing");
    }
}
