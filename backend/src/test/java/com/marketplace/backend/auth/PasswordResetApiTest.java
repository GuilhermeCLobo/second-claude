package com.marketplace.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.marketplace.backend.user.UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private void register(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username, "password", password, "email", username + "@example.com"))))
                .andExpect(status().isCreated());
    }

    private String requestResetAndCaptureToken(String username) throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username))))
                .andExpect(status().isOk());

        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        return tokenRepository.findByUserIdAndUsedAtIsNull(userId).stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow()
                .getToken();
    }

    @Test
    void requestingAResetThenConfirmingWithTheTokenLetsTheUserLogInWithTheNewPassword() throws Exception {
        register("pr-quinn", "correct-horse");
        String token = requestResetAndCaptureToken("pr-quinn");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "new-password1"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "pr-quinn", "password", "new-password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "pr-quinn", "password", "correct-horse"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestingAResetForAnUnknownUsernameRespondsIdenticallyToAKnownUsername() throws Exception {
        register("pr-rachel", "correct-horse");

        String knownResponse = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "pr-rachel"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String unknownResponse = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "pr-no-such-user"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(unknownResponse).isEqualTo(knownResponse);
    }

    @Test
    void confirmingWithAnExpiredTokenIsRejected() throws Exception {
        register("pr-sam", "correct-horse");
        String token = requestResetAndCaptureToken("pr-sam");

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow();
        ReflectionTestUtils.setField(resetToken, "expiresAt", Instant.now().minus(1, ChronoUnit.MINUTES));
        tokenRepository.save(resetToken);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "new-password1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void confirmingWithAnAlreadyUsedTokenIsRejected() throws Exception {
        register("pr-tina", "correct-horse");
        String token = requestResetAndCaptureToken("pr-tina");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "new-password1"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "another-password1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void requestingASecondResetInvalidatesTheFirstToken() throws Exception {
        register("pr-uma", "correct-horse");
        String firstToken = requestResetAndCaptureToken("pr-uma");
        String secondToken = requestResetAndCaptureToken("pr-uma");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", firstToken, "newPassword", "new-password1"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", secondToken, "newPassword", "new-password1"))))
                .andExpect(status().isOk());
    }

    @Test
    void confirmingWithAnUnknownTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", "no-such-token", "newPassword", "new-password1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void confirmingWithATooShortNewPasswordIsRejected() throws Exception {
        register("pr-victor", "correct-horse");
        String token = requestResetAndCaptureToken("pr-victor");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }
}
