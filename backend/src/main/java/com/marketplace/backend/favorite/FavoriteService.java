package com.marketplace.backend.favorite;

import com.marketplace.backend.listing.Listing;
import com.marketplace.backend.listing.ListingNotFoundException;
import com.marketplace.backend.listing.ListingRepository;
import com.marketplace.backend.listing.ListingResponse;
import com.marketplace.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ListingRepository listingRepository,
                            UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public ListingResponse addFavorite(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            favoriteRepository.save(new Favorite(userId, listingId));
        }
        return ListingResponse.from(listing, true, userRepository.usernameById(listing.getOwnerId()));
    }

    public void removeFavorite(Long listingId, Long userId) {
        favoriteRepository.findByUserIdAndListingId(userId, listingId)
                .ifPresent(favoriteRepository::delete);
    }

    public List<ListingResponse> myFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<Long, Listing> listingsById = listingRepository
                .findAllById(favorites.stream().map(Favorite::getListingId).toList())
                .stream()
                .collect(Collectors.toMap(Listing::getId, listing -> listing));
        Map<Long, String> ownerUsernames = userRepository.usernamesByIds(
                listingsById.values().stream().map(Listing::getOwnerId).toList());
        return favorites.stream()
                .map(favorite -> {
                    Listing listing = listingsById.get(favorite.getListingId());
                    return ListingResponse.from(listing, true, ownerUsernames.get(listing.getOwnerId()));
                })
                .toList();
    }
}
