package com.marketplace.backend.listing;

import com.marketplace.backend.auth.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/mine/posted")
    public List<ListingResponse> myPosted(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.myPosted(requesterId);
    }

    @GetMapping("/mine/bought")
    public List<ListingResponse> myBought(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.myBought(requesterId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestPart("listing") CreateListingRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        Long ownerId = currentUserResolver.resolve(authorization);
        ListingResponse response = listingService.create(request, photo, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ListingResponse edit(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody CreateListingRequest request) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.edit(id, request, requesterId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long requesterId = currentUserResolver.resolve(authorization);
        listingService.delete(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/buy")
    public ListingResponse buy(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.buy(id, requesterId);
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ListingResponse addPhoto(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.addPhoto(id, photo, requesterId);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ListingResponse removePhoto(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @PathVariable Long photoId) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.removePhoto(id, photoId, requesterId);
    }

    @PutMapping("/{id}/photos/order")
    public ListingResponse reorderPhotos(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ReorderPhotosRequest request) {
        Long requesterId = currentUserResolver.resolve(authorization);
        return listingService.reorderPhotos(id, request.photoIds(), requesterId);
    }
}
