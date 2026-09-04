package com.marketplace.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordResetRequest(
        @NotBlank(message = "must not be blank")
        String token,

        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 100, message = "must be at least 8 characters")
        String newPassword
) {
}
