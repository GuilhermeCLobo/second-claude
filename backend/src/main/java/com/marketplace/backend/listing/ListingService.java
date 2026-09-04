package com.marketplace.backend.listing;

import com.marketplace.backend.photo.PhotoStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private static final int MAX_PHOTOS = 6;

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
        Listing listing = new Listing(request.title(), request.description(), request.price(),
                request.category(), ownerId);
        listing.addPhoto(new Photo(listing, storeReference(photo), 0));
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

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

    public ListingResponse edit(Long id, CreateListingRequest request, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        listing.update(request.title(), request.description(), request.price(), request.category());
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

    public void delete(Long id, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        listingRepository.delete(listing);
    }

    public ListingResponse addPhoto(Long id, MultipartFile photo, Long requesterId) {
        if (photo == null || photo.isEmpty()) {
            throw new MissingPhotoException();
        }
        Listing listing = ownedActiveListing(id, requesterId);
        if (listing.getPhotos().size() >= MAX_PHOTOS) {
            throw new PhotoLimitExceededException();
        }
        listing.addPhoto(new Photo(listing, storeReference(photo), listing.getPhotos().size()));
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

    public ListingResponse removePhoto(Long id, Long photoId, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        if (listing.getPhotos().size() <= 1) {
            throw new LastPhotoException();
        }
        Photo photo = listing.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ListingPhotoNotFoundException(photoId));
        listing.removePhoto(photo);
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

    public ListingResponse reorderPhotos(Long id, List<Long> photoIds, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        Set<Long> currentIds = listing.getPhotos().stream().map(Photo::getId).collect(Collectors.toSet());
        if (photoIds.size() != currentIds.size() || !currentIds.equals(new HashSet<>(photoIds))) {
            throw new InvalidPhotoOrderException();
        }
        Map<Long, Photo> photosById = listing.getPhotos().stream()
                .collect(Collectors.toMap(Photo::getId, photo -> photo));
        for (int i = 0; i < photoIds.size(); i++) {
            photosById.get(photoIds.get(i)).setSortOrder(i);
        }
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved);
    }

    private Listing ownedActiveListing(Long id, Long requesterId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        if (!listing.getOwnerId().equals(requesterId)) {
            throw new NotListingOwnerException();
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingNotActiveException();
        }
        return listing;
    }

    private String storeReference(MultipartFile photo) {
        return "/api/photos/" + photoStore.store(photo);
    }
}
