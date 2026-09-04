package com.marketplace.backend.listing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ListingExceptionHandler {

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleListingNotFound(ListingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MissingPhotoException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPhoto(MissingPhotoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Validation failed", "errors", Map.of("photo", "must not be blank")));
    }

    @ExceptionHandler(NotListingOwnerException.class)
    public ResponseEntity<Map<String, String>> handleNotListingOwner(NotListingOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ListingNotActiveException.class)
    public ResponseEntity<Map<String, String>> handleListingNotActive(ListingNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
