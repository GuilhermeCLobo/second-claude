package com.marketplace.backend.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private record Session(String token, Long userId) {
    }

    private Session registerAndLoginSession(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username, "password", "correct-horse", "email", username + "@example.com"))));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "correct-horse"))))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new Session(json.get("token").asText(), json.get("userId").asLong());
    }

    private Long createListing(String token, String title) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/listings")
                        .file(new org.springframework.mock.web.MockMultipartFile("listing", "", "application/json",
                                objectMapper.writeValueAsBytes(Map.of(
                                        "title", title, "description", "A description",
                                        "price", new BigDecimal("40.00"), "category", "FURNITURE"))))
                        .file(new org.springframework.mock.web.MockMultipartFile("photo", "photo.jpg", "image/jpeg",
                                "fake-photo-bytes".getBytes()))
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void profileForAnExistingUsernameReturnsUsernameAndActiveListings() throws Exception {
        Session owner = registerAndLoginSession("profile-owner");
        createListing(owner.token(), "Chair");

        mockMvc.perform(get("/api/users/{username}", "profile-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profile-owner"))
                .andExpect(jsonPath("$.listings.length()").value(1))
                .andExpect(jsonPath("$.listings[0].title").value("Chair"))
                .andExpect(jsonPath("$.listings[0].ownerUsername").value("profile-owner"));
    }

    @Test
    void profileExcludesSoldListings() throws Exception {
        Session owner = registerAndLoginSession("profile-owner-sold");
        Session buyer = registerAndLoginSession("profile-buyer-sold");
        Long soldId = createListing(owner.token(), "Sold Item");
        createListing(owner.token(), "Active Item");

        mockMvc.perform(post("/api/listings/{id}/buy", soldId)
                        .header("Authorization", "Bearer " + buyer.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{username}", "profile-owner-sold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings.length()").value(1))
                .andExpect(jsonPath("$.listings[0].title").value("Active Item"));
    }

    @Test
    void profileForANonexistentUsernameReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{username}", "no-such-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void profileIsAccessibleWithoutAuthentication() throws Exception {
        registerAndLoginSession("profile-public");

        mockMvc.perform(get("/api/users/{username}", "profile-public"))
                .andExpect(status().isOk());
    }

    @Test
    void profileListingsReflectFavoritedStatusForTheAuthenticatedRequester() throws Exception {
        Session owner = registerAndLoginSession("profile-owner-fav");
        Session fan = registerAndLoginSession("profile-fan");
        Long listingId = createListing(owner.token(), "Bike");

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{username}", "profile-owner-fav")
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(jsonPath("$.listings[0].favorited").value(true));

        mockMvc.perform(get("/api/users/{username}", "profile-owner-fav"))
                .andExpect(jsonPath("$.listings[0].favorited").value(false));
    }
}
