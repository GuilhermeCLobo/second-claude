package com.marketplace.backend.favorite;

import com.marketplace.backend.auth.CurrentUserResolver;
import com.marketplace.backend.listing.ListingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CurrentUserResolver currentUserResolver;

    public FavoriteController(FavoriteService favoriteService, CurrentUserResolver currentUserResolver) {
        this.favoriteService = favoriteService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping("/{id}/favorite")
    public ListingResponse addFavorite(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return favoriteService.addFavorite(id, requesterId);
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long requesterId = currentUserResolver.resolve(authorization);
        favoriteService.removeFavorite(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine/favorites")
    public List<ListingResponse> myFavorites(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return favoriteService.myFavorites(requesterId);
    }
}
