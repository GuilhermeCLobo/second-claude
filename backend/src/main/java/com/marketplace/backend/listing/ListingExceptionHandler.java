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
}
