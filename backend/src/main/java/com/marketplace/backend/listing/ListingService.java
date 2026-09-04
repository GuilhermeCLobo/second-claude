package com.marketplace.backend.listing;

import com.marketplace.backend.photo.PhotoStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final PhotoStore photoStore;

    public ListingService(ListingRepository listingRepository, PhotoStore photoStore) {
        this.listingRepository = listingRepository;
        this.photoStore = photoStore;
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

    public List<ListingResponse> myPosted(Long ownerId) {
        return listingRepository.findByOwnerId(ownerId).stream().map(ListingResponse::from).toList();
    }

    public List<ListingResponse> myBought(Long buyerId) {
        return listingRepository.findByBuyerId(buyerId).stream().map(ListingResponse::from).toList();
    }

    public ListingResponse create(CreateListingRequest request, MultipartFile photo, Long ownerId) {
        if (photo == null || photo.isEmpty()) {
            throw new MissingPhotoException();
        }
        String photoReference = "/api/photos/" + photoStore.store(photo);
        Listing listing = new Listing(request.title(), request.description(), request.price(),
                request.category(), photoReference, ownerId);
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

    @Transactional
    public ListingResponse buy(Long id, Long requesterId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        if (listing.getOwnerId().equals(requesterId)) {
            throw new CannotBuyOwnListingException();
        }
        int updated = listingRepository.markSoldIfActive(id, requesterId);
        if (updated == 0) {
            throw new ListingNotActiveException();
        }
        Listing sold = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        return ListingResponse.from(sold);
    }

    public void delete(Long id, Long requesterId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        if (!listing.getOwnerId().equals(requesterId)) {
            throw new NotListingOwnerException();
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingNotActiveException();
        }
        listingRepository.delete(listing);
    }
}
