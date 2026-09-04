package com.marketplace.backend.listing;

import org.springframework.data.domain.Page;

import java.util.List;

public record BrowseListingsResponse(List<ListingResponse> listings, long totalCount) {
    static BrowseListingsResponse from(Page<Listing> page) {
        return new BrowseListingsResponse(
                page.getContent().stream().map(ListingResponse::from).toList(),
                page.getTotalElements());
    }
}
