package com.marketplace.backend.auth;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final JwtService jwtService;

    public CurrentUserResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Long resolve(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthenticatedException();
        }
        String token = authorizationHeader.substring("Bearer ".length());
        try {
            return jwtService.extractUserId(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthenticatedException();
        }
    }
}
