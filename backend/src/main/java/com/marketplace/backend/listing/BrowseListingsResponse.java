package com.marketplace.backend.listing;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public record BrowseListingsResponse(List<ListingResponse> listings, long totalCount) {
    static BrowseListingsResponse from(Page<Listing> page, Set<Long> favoritedListingIds) {
        return new BrowseListingsResponse(
                page.getContent().stream()
                        .map(listing -> ListingResponse.from(listing, favoritedListingIds.contains(listing.getId())))
                        .toList(),
                page.getTotalElements());
    }
}
