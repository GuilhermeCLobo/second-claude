package com.marketplace.backend.listing;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<ListingResponse> browse(Category category) {
        List<Listing> listings = category == null
                ? listingRepository.findAll()
                : listingRepository.findByCategory(category);
        return listings.stream().map(ListingResponse::from).toList();
    }
}
