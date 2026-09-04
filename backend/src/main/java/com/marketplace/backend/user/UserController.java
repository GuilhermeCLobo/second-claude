package com.marketplace.backend.user;

import com.marketplace.backend.auth.CurrentUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ProfileService profileService;
    private final CurrentUserResolver currentUserResolver;

    public UserController(ProfileService profileService, CurrentUserResolver currentUserResolver) {
        this.profileService = profileService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/{username}")
    public UserProfileResponse getProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String username) {
        Long requesterId = currentUserResolver.resolveOptional(authorization);
        return profileService.getProfile(username, requesterId);
    }
}
