package com.marketplace.backend.favorite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavoritesApiTest {

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

    private Long createListing(String token) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/listings")
                        .file(new org.springframework.mock.web.MockMultipartFile("listing", "", "application/json",
                                objectMapper.writeValueAsBytes(Map.of(
                                        "title", "Chair", "description", "Office chair",
                                        "price", new BigDecimal("40.00"), "category", "FURNITURE"))))
                        .file(new org.springframework.mock.web.MockMultipartFile("photo", "chair.jpg", "image/jpeg",
                                "fake-photo-bytes".getBytes()))
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void favoritingWithoutAuthenticationIsRejected() throws Exception {
        Session owner = registerAndLoginSession("owner-noauth");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unfavoritingWithoutAuthenticationIsRejected() throws Exception {
        Session owner = registerAndLoginSession("owner-unfav-noauth");
        Long listingId = createListing(owner.token());

        mockMvc.perform(delete("/api/listings/{id}/favorite", listingId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void fetchingMyFavoritesWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/api/listings/mine/favorites"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void favoritingAListingMarksItFavoritedInSubsequentResponses() throws Exception {
        Session owner = registerAndLoginSession("owner-favorite");
        Session fan = registerAndLoginSession("fan-favorite");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));

        mockMvc.perform(get("/api/listings/{id}", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(jsonPath("$.favorited").value(true));

        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(jsonPath("$.favorited").value(false));
    }

    @Test
    void favoritingTheSameListingTwiceDoesNotCreateADuplicate() throws Exception {
        Session owner = registerAndLoginSession("owner-dup");
        Session fan = registerAndLoginSession("fan-dup");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings/mine/favorites")
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void unfavoritingRemovesItFromMyFavorites() throws Exception {
        Session owner = registerAndLoginSession("owner-unfav");
        Session fan = registerAndLoginSession("fan-unfav");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listings/mine/favorites")
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unfavoritingSomethingNeverFavoritedIsANoOp() throws Exception {
        Session owner = registerAndLoginSession("owner-unfav-noop");
        Session fan = registerAndLoginSession("fan-unfav-noop");
        Long listingId = createListing(owner.token());

        mockMvc.perform(delete("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isNoContent());
    }

    @Test
    void aUserCanFavoriteTheirOwnListing() throws Exception {
        Session owner = registerAndLoginSession("owner-self-favorite");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void aUserCanFavoriteASoldListing() throws Exception {
        Session owner = registerAndLoginSession("owner-sold-favorite");
        Session buyer = registerAndLoginSession("buyer-sold-favorite");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/buy", listingId)
                        .header("Authorization", "Bearer " + buyer.token()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + buyer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void deletingAListingRemovesItsFavorites() throws Exception {
        Session owner = registerAndLoginSession("owner-delete-favorite");
        Session fan = registerAndLoginSession("fan-delete-favorite");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listings/mine/favorites")
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myFavoritesReturnsOnlyTheCurrentUsersFavorites() throws Exception {
        Session owner = registerAndLoginSession("owner-mine-favorites");
        Session fanOne = registerAndLoginSession("fan-one-mine-favorites");
        Session fanTwo = registerAndLoginSession("fan-two-mine-favorites");
        Long listingId = createListing(owner.token());

        mockMvc.perform(post("/api/listings/{id}/favorite", listingId)
                        .header("Authorization", "Bearer " + fanOne.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings/mine/favorites")
                        .header("Authorization", "Bearer " + fanOne.token()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(listingId));

        mockMvc.perform(get("/api/listings/mine/favorites")
                        .header("Authorization", "Bearer " + fanTwo.token()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void favoritingAnUnknownListingReturnsNotFound() throws Exception {
        Session fan = registerAndLoginSession("fan-unknown-listing");

        mockMvc.perform(post("/api/listings/{id}/favorite", 999999)
                        .header("Authorization", "Bearer " + fan.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
