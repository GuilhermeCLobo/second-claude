package com.marketplace.backend.listing;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record ListingResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        Category category,
        List<PhotoResponse> photos,
        ListingStatus status,
        Long ownerId,
        Long buyerId,
        boolean favorited
) {
    public static ListingResponse from(Listing listing, boolean favorited) {
        List<PhotoResponse> photos = listing.getPhotos().stream()
                .sorted(Comparator.comparingInt(Photo::getSortOrder))
                .map(PhotoResponse::from)
                .toList();
        return new ListingResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCategory(),
                photos,
                listing.getStatus(),
                listing.getOwnerId(),
                listing.getBuyerId(),
                favorited
        );
    }

    public record PhotoResponse(Long id, String reference) {
        static PhotoResponse from(Photo photo) {
            return new PhotoResponse(photo.getId(), photo.getReference());
        }
    }
}
