package com.marketplace.backend.listing;

import java.math.BigDecimal;

public record ListingResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        Category category,
        String photoReference,
        ListingStatus status,
        Long ownerId,
        Long buyerId
) {
    static ListingResponse from(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCategory(),
                listing.getPhotoReference(),
                listing.getStatus(),
                listing.getOwnerId(),
                listing.getBuyerId()
        );
    }
}
