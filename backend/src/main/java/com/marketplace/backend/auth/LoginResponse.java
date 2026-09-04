package com.marketplace.backend.auth;

public record LoginResponse(String token, Long userId, String username) {
}
