package com.marketplace.backend.listing;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderPhotosRequest(
        @NotEmpty(message = "must not be empty")
        List<Long> photoIds
) {
}
