package com.marketplace.backend.auth;

import com.marketplace.backend.email.EmailSender;
import com.marketplace.backend.user.User;
import com.marketplace.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    @Transactional
    public void requestReset(RequestPasswordResetRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(this::issueResetToken);
    }

    @Transactional
    public void confirmReset(ConfirmPasswordResetRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(InvalidResetTokenException::new);
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidResetTokenException();
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(InvalidResetTokenException::new);
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.markUsed(Instant.now());
    }

    private void issueResetToken(User user) {
        tokenRepository.deleteAll(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId()));

        Instant now = Instant.now();
        PasswordResetToken resetToken = new PasswordResetToken(
                user.getId(), generateToken(), now.plus(TOKEN_TTL), now);
        tokenRepository.save(resetToken);

        emailSender.send(user.getEmail(), "Reset your password",
                "Use this token to reset your password: " + resetToken.getToken());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
