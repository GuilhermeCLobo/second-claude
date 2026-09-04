package com.marketplace.backend.user;

import com.marketplace.backend.favorite.FavoriteRepository;
import com.marketplace.backend.listing.Listing;
import com.marketplace.backend.listing.ListingRepository;
import com.marketplace.backend.listing.ListingResponse;
import com.marketplace.backend.listing.ListingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;

    public ProfileService(UserRepository userRepository, ListingRepository listingRepository,
                           FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public UserProfileResponse getProfile(String username, Long requesterId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        List<Listing> listings = listingRepository.findByOwnerIdAndStatus(user.getId(), ListingStatus.ACTIVE);
        Set<Long> favoritedIds = favoritedListingIds(listings, requesterId);
        List<ListingResponse> listingResponses = listings.stream()
                .map(listing -> ListingResponse.from(listing, favoritedIds.contains(listing.getId()), user.getUsername()))
                .toList();
        return new UserProfileResponse(user.getUsername(), listingResponses);
    }

    private Set<Long> favoritedListingIds(List<Listing> listings, Long requesterId) {
        if (requesterId == null || listings.isEmpty()) {
            return Set.of();
        }
        return favoriteRepository.findFavoritedListingIds(requesterId, listings.stream().map(Listing::getId).toList());
    }
}
