package com.marketplace.backend.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListingsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListingRepository listingRepository;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "correct-horse"))));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "correct-horse"))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void browsingWithoutACategoryReturnsAllListingsIncludingSoldOnes() throws Exception {
        listingRepository.save(new Listing("Bike", "Road bike", new BigDecimal("150.00"),
                Category.VEHICLES, "bike.jpg", 1L));
        Listing soldListing = listingRepository.save(new Listing("Sofa", "Comfy sofa", new BigDecimal("300.00"),
                Category.FURNITURE, "sofa.jpg", 1L));
        ReflectionTestUtils.setField(soldListing, "status", ListingStatus.SOLD);
        listingRepository.save(soldListing);

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Bike')].status").value("ACTIVE"))
                .andExpect(jsonPath("$[?(@.title == 'Sofa')].status").value("SOLD"));
    }

    @Test
    void browsingWithACategoryFiltersToJustThatCategory() throws Exception {
        listingRepository.save(new Listing("Laptop", "Fast laptop", new BigDecimal("800.00"),
                Category.ELECTRONICS, "laptop.jpg", 2L));
        listingRepository.save(new Listing("Desk", "Wooden desk", new BigDecimal("120.00"),
                Category.FURNITURE, "desk.jpg", 2L));

        mockMvc.perform(get("/api/listings").param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItem("Laptop")))
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Desk"))));
    }

    @Test
    void gettingAnExistingListingByIdReturnsItsFullDetail() throws Exception {
        Listing listing = listingRepository.save(new Listing("Bike", "Road bike", new BigDecimal("150.00"),
                Category.VEHICLES, "bike.jpg", 1L));

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listing.getId()))
                .andExpect(jsonPath("$.title").value("Bike"))
                .andExpect(jsonPath("$.description").value("Road bike"))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.category").value("VEHICLES"))
                .andExpect(jsonPath("$.photoReference").value("bike.jpg"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void gettingAListingByAnUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/listings/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creatingAListingWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(post("/api/listings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Camera", "description", "Digital camera",
                                "price", "150.00", "category", "ELECTRONICS"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creatingAListingWithMissingFieldsIsRejectedWithValidationErrors() throws Exception {
        String token = registerAndLogin("frank");

        mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "", "description", "", "category", "VEHICLES"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void creatingAListingWhenLoggedInPersistsItAsActiveOwnedByTheCreatorAndMakesItBrowsableAndViewable()
            throws Exception {
        String token = registerAndLogin("grace");

        String response = mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Vintage Camera", "description", "Digital camera",
                                "price", "150.00", "category", "ELECTRONICS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Vintage Camera"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ownerId").exists())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/listings"))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].title").value("Vintage Camera"));

        mockMvc.perform(get("/api/listings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Vintage Camera"));
    }
}
