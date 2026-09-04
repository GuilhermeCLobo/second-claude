package com.marketplace.backend.listing;

import com.marketplace.backend.favorite.FavoriteRepository;
import com.marketplace.backend.photo.PhotoStore;
import com.marketplace.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private static final int MAX_PHOTOS = 6;
    static final int DEFAULT_PAGE_SIZE = 12;
    static final int MAX_PAGE_SIZE = 48;

    private final ListingRepository listingRepository;
    private final PhotoStore photoStore;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public ListingService(ListingRepository listingRepository, PhotoStore photoStore,
                           FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.photoStore = photoStore;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    public BrowseListingsResponse browse(Category category, String search, BigDecimal minPrice, BigDecimal maxPrice,
                                          ListingSortOption sort, int page, int size, Long requesterId) {
        Specification<Listing> specification = ListingSpecifications.and(
                ListingSpecifications.statusIs(ListingStatus.ACTIVE),
                ListingSpecifications.categoryIs(category),
                ListingSpecifications.matchesSearch(search),
                ListingSpecifications.priceAtLeast(minPrice),
                ListingSpecifications.priceAtMost(maxPrice));

        int pageSize = Math.min(size < 1 ? DEFAULT_PAGE_SIZE : size, MAX_PAGE_SIZE);
        int pageNumber = Math.max(page, 0);
        Page<Listing> result = listingRepository.findAll(specification, PageRequest.of(pageNumber, pageSize, sortFor(sort)));
        Set<Long> favoritedIds = favoritedListingIds(
                result.getContent().stream().map(Listing::getId).toList(), requesterId);
        Map<Long, String> ownerUsernames = userRepository.usernamesByIds(
                result.getContent().stream().map(Listing::getOwnerId).toList());
        return BrowseListingsResponse.from(result, favoritedIds, ownerUsernames);
    }

    private Sort sortFor(ListingSortOption sort) {
        ListingSortOption effective = sort == null ? ListingSortOption.NEWEST : sort;
        return switch (effective) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.ASC, "id"));
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.ASC, "id"));
        };
    }

    public ListingResponse getById(Long id, Long requesterId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException(id));
        return ListingResponse.from(listing, isFavorited(id, requesterId), userRepository.usernameById(listing.getOwnerId()));
    }

    public List<ListingResponse> myPosted(Long ownerId) {
        List<Listing> listings = listingRepository.findByOwnerId(ownerId);
        Set<Long> favoritedIds = favoritedListingIds(listings.stream().map(Listing::getId).toList(), ownerId);
        String ownerUsername = userRepository.usernameById(ownerId);
        return listings.stream()
                .map(listing -> ListingResponse.from(listing, favoritedIds.contains(listing.getId()), ownerUsername))
                .toList();
    }

    public List<ListingResponse> myBought(Long buyerId) {
        List<Listing> listings = listingRepository.findByBuyerId(buyerId);
        Set<Long> favoritedIds = favoritedListingIds(listings.stream().map(Listing::getId).toList(), buyerId);
        Map<Long, String> ownerUsernames = userRepository.usernamesByIds(
                listings.stream().map(Listing::getOwnerId).toList());
        return listings.stream()
                .map(listing -> ListingResponse.from(listing, favoritedIds.contains(listing.getId()),
                        ownerUsernames.get(listing.getOwnerId())))
                .toList();
    }

    public ListingResponse create(CreateListingRequest request, MultipartFile photo, Long ownerId) {
        if (photo == null || photo.isEmpty()) {
            throw new MissingPhotoException();
        }
        Listing listing = new Listing(request.title(), request.description(), request.price(),
                request.category(), ownerId);
        listing.addPhoto(new Photo(listing, storeReference(photo), 0));
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved, false, userRepository.usernameById(ownerId));
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
        return ListingResponse.from(sold, isFavorited(id, requesterId), userRepository.usernameById(sold.getOwnerId()));
    }

    public ListingResponse edit(Long id, CreateListingRequest request, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        listing.update(request.title(), request.description(), request.price(), request.category());
        Listing saved = listingRepository.save(listing);
        return ListingResponse.from(saved, isFavorited(id, requesterId), userRepository.usernameById(saved.getOwnerId()));
    }

    public void delete(Long id, Long requesterId) {
        Listing listing = ownedActiveListing(id, requesterId);
        listingRepository.delete(listing);
        favoriteRepository.deleteByListingId(id);
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
        return ListingResponse.from(saved, isFavorited(id, requesterId), userRepository.usernameById(saved.getOwnerId()));
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
        return ListingResponse.from(saved, isFavorited(id, requesterId), userRepository.usernameById(saved.getOwnerId()));
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
        return ListingResponse.from(saved, isFavorited(id, requesterId), userRepository.usernameById(saved.getOwnerId()));
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

    private boolean isFavorited(Long listingId, Long requesterId) {
        return requesterId != null && favoriteRepository.existsByUserIdAndListingId(requesterId, listingId);
    }

    private Set<Long> favoritedListingIds(List<Long> listingIds, Long requesterId) {
        if (requesterId == null || listingIds.isEmpty()) {
            return Set.of();
        }
        return favoriteRepository.findFavoritedListingIds(requesterId, listingIds);
    }

    private String storeReference(MultipartFile photo) {
        return "/api/photos/" + photoStore.store(photo);
    }
}
