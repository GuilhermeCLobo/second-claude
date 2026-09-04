package com.marketplace.backend.user;

import com.marketplace.backend.listing.ListingResponse;

import java.util.List;

public record UserProfileResponse(String username, List<ListingResponse> listings) {
}
