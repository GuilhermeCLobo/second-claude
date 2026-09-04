package com.marketplace.backend.listing;

public class ListingPhotoNotFoundException extends RuntimeException {

    public ListingPhotoNotFoundException(Long photoId) {
        super("No photo found with id " + photoId + " on this listing");
    }
}
