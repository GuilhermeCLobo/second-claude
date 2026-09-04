package com.marketplace.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "must not be blank")
        String username,

        @NotBlank(message = "must not be blank")
        String password
) {
}
