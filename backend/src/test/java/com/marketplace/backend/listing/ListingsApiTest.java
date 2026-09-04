package com.marketplace.backend.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    private record Session(String token, Long userId) {
    }

    private String registerAndLogin(String username) throws Exception {
        return registerAndLoginSession(username).token();
    }

    private Session registerAndLoginSession(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "correct-horse"))));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "correct-horse"))))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(response);
        return new Session(json.get("token").asText(), json.get("userId").asLong());
    }

    private MockMultipartFile listingPart(Map<String, Object> fields) throws Exception {
        return new MockMultipartFile("listing", "", "application/json",
                objectMapper.writeValueAsBytes(fields));
    }

    private MockMultipartFile photoPart() {
        return new MockMultipartFile("photo", "camera.jpg", "image/jpeg", "fake-photo-bytes".getBytes(StandardCharsets.UTF_8));
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
        mockMvc.perform(multipart("/api/listings")
                        .file(listingPart(Map.of(
                                "title", "Camera", "description", "Digital camera",
                                "price", "150.00", "category", "ELECTRONICS")))
                        .file(photoPart()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creatingAListingWithMissingFieldsIsRejectedWithValidationErrors() throws Exception {
        String token = registerAndLogin("frank");

        mockMvc.perform(multipart("/api/listings")
                        .file(listingPart(Map.of(
                                "title", "", "description", "", "category", "VEHICLES")))
                        .file(photoPart())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void creatingAListingWithoutAPhotoIsRejectedWithAValidationError() throws Exception {
        String token = registerAndLogin("holly");

        mockMvc.perform(multipart("/api/listings")
                        .file(listingPart(Map.of(
                                "title", "Camera", "description", "Digital camera",
                                "price", "150.00", "category", "ELECTRONICS")))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.photo").exists());
    }

    @Test
    void creatingAListingWhenLoggedInPersistsItAsActiveOwnedByTheCreatorAndMakesItBrowsableAndViewable()
            throws Exception {
        String token = registerAndLogin("grace");

        String response = mockMvc.perform(multipart("/api/listings")
                        .file(listingPart(Map.of(
                                "title", "Vintage Camera", "description", "Digital camera",
                                "price", "150.00", "category", "ELECTRONICS")))
                        .file(photoPart())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Vintage Camera"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ownerId").exists())
                .andExpect(jsonPath("$.photoReference").value(org.hamcrest.Matchers.startsWith("/api/photos/")))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();
        String photoReference = objectMapper.readTree(response).get("photoReference").asText();

        mockMvc.perform(get("/api/listings"))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].title").value("Vintage Camera"));

        mockMvc.perform(get("/api/listings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Vintage Camera"));

        mockMvc.perform(get(photoReference))
                .andExpect(status().isOk())
                .andExpect(content().bytes("fake-photo-bytes".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void requestingAnUnknownPhotoReferenceReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/photos/{reference}", "does-not-exist.jpg"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(new Listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingAnotherUsersListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("irene");
        Listing listing = listingRepository.save(new Listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("jack");
        Listing listing = listingRepository.save(new Listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));
        ReflectionTestUtils.setField(listing, "status", ListingStatus.SOLD);
        listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingYourOwnActiveListingRemovesItSoItNoLongerAppearsWhenBrowsingOrViewing() throws Exception {
        Session session = registerAndLoginSession("karen");
        Listing listing = listingRepository.save(new Listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void buyingWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(new Listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", 1L));

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void buyingYourOwnListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("liam");
        Listing listing = listingRepository.save(new Listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", session.userId()));

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void buyingAnAlreadySoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("mona");
        Listing listing = listingRepository.save(new Listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", session.userId() + 1));
        ReflectionTestUtils.setField(listing, "status", ListingStatus.SOLD);
        listingRepository.save(listing);

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void buyingAnActiveListingOwnedBySomeoneElseMarksItSoldWithTheBuyerRecorded() throws Exception {
        Session session = registerAndLoginSession("nina");
        Listing listing = listingRepository.save(new Listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", session.userId() + 1));

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"))
                .andExpect(jsonPath("$.buyerId").value(session.userId()));

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(jsonPath("$.status").value("SOLD"))
                .andExpect(jsonPath("$.buyerId").value(session.userId()));
    }

    @Test
    void concurrentBuyAttemptsOnTheSameListingResultInExactlyOneWinner() throws Exception {
        Session owner = registerAndLoginSession("owen-concurrent");
        Session buyerA = registerAndLoginSession("buyer-a-concurrent");
        Session buyerB = registerAndLoginSession("buyer-b-concurrent");
        Listing listing = listingRepository.save(new Listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", owner.userId()));

        Callable<Integer> attemptA = () -> mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + buyerA.token()))
                .andReturn().getResponse().getStatus();
        Callable<Integer> attemptB = () -> mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + buyerB.token()))
                .andReturn().getResponse().getStatus();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> resultA = executor.submit(attemptA);
            Future<Integer> resultB = executor.submit(attemptB);
            int statusA = resultA.get(5, TimeUnit.SECONDS);
            int statusB = resultB.get(5, TimeUnit.SECONDS);

            assertThat(List.of(statusA, statusB)).containsExactlyInAnyOrder(200, 409);
        } finally {
            executor.shutdown();
        }

        Listing afterRace = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(afterRace.getStatus()).isEqualTo(ListingStatus.SOLD);
        assertThat(afterRace.getBuyerId()).isIn(buyerA.userId(), buyerB.userId());
    }

    @Test
    void fetchingMyPostedListingsWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/api/listings/mine/posted"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void fetchingMyBoughtListingsWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/api/listings/mine/bought"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void myPostedListingsReturnsOnlyListingsOwnedByTheCurrentUser() throws Exception {
        Session session = registerAndLoginSession("oscar");
        listingRepository.save(new Listing("Guitar", "Acoustic guitar", new BigDecimal("90.00"),
                Category.OTHER, "guitar.jpg", session.userId()));
        listingRepository.save(new Listing("Drone", "Camera drone", new BigDecimal("250.00"),
                Category.ELECTRONICS, "drone.jpg", session.userId() + 1));

        mockMvc.perform(get("/api/listings/mine/posted")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItem("Guitar")))
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Drone"))))
                .andExpect(jsonPath("$[*].ownerId", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.equalTo(session.userId().intValue()))));
    }

    @Test
    void myBoughtListingsReturnsOnlyListingsBoughtByTheCurrentUser() throws Exception {
        Session session = registerAndLoginSession("paula");
        Listing bought = listingRepository.save(new Listing("Tablet", "10-inch tablet", new BigDecimal("200.00"),
                Category.ELECTRONICS, "tablet.jpg", session.userId() + 1));
        ReflectionTestUtils.setField(bought, "status", ListingStatus.SOLD);
        ReflectionTestUtils.setField(bought, "buyerId", session.userId());
        listingRepository.save(bought);
        listingRepository.save(new Listing("Rug", "Wool rug", new BigDecimal("60.00"),
                Category.HOME_AND_GARDEN, "rug.jpg", session.userId() + 1));

        mockMvc.perform(get("/api/listings/mine/bought")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItem("Tablet")))
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Rug"))))
                .andExpect(jsonPath("$[*].buyerId", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.equalTo(session.userId().intValue()))));
    }
}
