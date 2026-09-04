package com.marketplace.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 50, message = "must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 100, message = "must be at least 8 characters")
        String password
) {
}
