package com.marketplace.backend.listing;

import com.marketplace.backend.auth.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;
    private final CurrentUserResolver currentUserResolver;

    public ListingController(ListingService listingService, CurrentUserResolver currentUserResolver) {
        this.listingService = listingService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<ListingResponse> browse(@RequestParam(required = false) Category category) {
        return listingService.browse(category);
    }

    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable Long id) {
        return listingService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateListingRequest request) {
        Long ownerId = currentUserResolver.resolve(authorization);
        ListingResponse response = listingService.create(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
