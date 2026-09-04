package com.marketplace.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetRequest(
        @NotBlank(message = "must not be blank")
        String username
) {
}
