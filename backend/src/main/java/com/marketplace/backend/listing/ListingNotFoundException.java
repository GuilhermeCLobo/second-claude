package com.marketplace.backend.listing;

public class ListingNotFoundException extends RuntimeException {

    public ListingNotFoundException(Long id) {
        super("No listing found with id " + id);
    }
}
