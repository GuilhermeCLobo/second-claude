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

    public ListingResponse getById(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        return ListingResponse.from(listing);
    }

    public ListingResponse create(CreateListingRequest request, Long ownerId) {
        Listing listing = new Listing(request.title(), request.description(), request.price(),
                request.category(), null, ownerId);
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }
}
