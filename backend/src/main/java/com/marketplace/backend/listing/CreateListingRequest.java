package com.marketplace.backend.listing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateListingRequest(
        @NotBlank(message = "must not be blank")
        String title,

        @NotBlank(message = "must not be blank")
        String description,

        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.01", message = "must be greater than zero")
        BigDecimal price,

        @NotNull(message = "must not be null")
        Category category
) {
}
