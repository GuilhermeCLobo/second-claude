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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username, "password", "correct-horse", "email", username + "@example.com"))));

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

    private Listing listing(String title, String description, BigDecimal price, Category category,
                             String photoReference, Long ownerId) {
        Listing listing = new Listing(title, description, price, category, ownerId);
        listing.addPhoto(new Photo(listing, photoReference, 0));
        return listing;
    }

    @Test
    void browsingByDefaultExcludesSoldListings() throws Exception {
        listingRepository.save(listing("Bike", "Road bike", new BigDecimal("150.00"),
                Category.VEHICLES, "bike.jpg", 1L));
        Listing soldListing = listingRepository.save(listing("Sofa", "Comfy sofa", new BigDecimal("300.00"),
                Category.FURNITURE, "sofa.jpg", 1L));
        ReflectionTestUtils.setField(soldListing, "status", ListingStatus.SOLD);
        listingRepository.save(soldListing);

        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.hasItem("Bike")))
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Sofa"))));
    }

    @Test
    void browsingWithACategoryFiltersToJustThatCategory() throws Exception {
        listingRepository.save(listing("Laptop", "Fast laptop", new BigDecimal("800.00"),
                Category.ELECTRONICS, "laptop.jpg", 2L));
        listingRepository.save(listing("Desk", "Wooden desk", new BigDecimal("120.00"),
                Category.FURNITURE, "desk.jpg", 2L));

        mockMvc.perform(get("/api/listings").param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.hasItem("Laptop")))
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Desk"))));
    }

    @Test
    void searchMatchesCaseInsensitivelyAgainstTitleOnly() throws Exception {
        listingRepository.save(listing("Vintage Lensmaster9000", "Old but working", new BigDecimal("80.00"),
                Category.ELECTRONICS, "camera.jpg", 3L));
        listingRepository.save(listing("Desk", "Wooden desk", new BigDecimal("120.00"),
                Category.FURNITURE, "desk.jpg", 3L));

        mockMvc.perform(get("/api/listings").param("search", "LENSMASTER9000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.contains("Vintage Lensmaster9000")))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void searchMatchesCaseInsensitivelyAgainstDescriptionOnly() throws Exception {
        listingRepository.save(listing("Old Photo Equipment", "A vintage SHUTTERPRO7000 in good shape",
                new BigDecimal("80.00"), Category.ELECTRONICS, "camera.jpg", 3L));
        listingRepository.save(listing("Desk", "Wooden desk", new BigDecimal("120.00"),
                Category.FURNITURE, "desk.jpg", 3L));

        mockMvc.perform(get("/api/listings").param("search", "shutterpro7000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.contains("Old Photo Equipment")))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void searchMatchingNeitherTitleNorDescriptionReturnsNoResults() throws Exception {
        listingRepository.save(listing("Desk", "Wooden desk", new BigDecimal("120.00"),
                Category.FURNITURE, "desk.jpg", 3L));

        mockMvc.perform(get("/api/listings").param("search", "unobtainium1975xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings").isArray())
                .andExpect(jsonPath("$.listings.length()").value(0))
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    void priceRangeFiltersCombineWithMinOnlyMaxOnlyBothAndNeitherAndWithSearch() throws Exception {
        listingRepository.save(listing("Cheap Pricewidget", "A cheap item", new BigDecimal("10.00"),
                Category.OTHER, "cheap.jpg", 4L));
        listingRepository.save(listing("Mid Pricewidget", "A mid-priced item", new BigDecimal("50.00"),
                Category.OTHER, "mid.jpg", 4L));
        listingRepository.save(listing("Pricey Pricewidget", "An expensive item", new BigDecimal("100.00"),
                Category.OTHER, "pricey.jpg", 4L));

        mockMvc.perform(get("/api/listings").param("search", "Pricewidget").param("minPrice", "40"))
                .andExpect(jsonPath("$.listings[*].title",
                        org.hamcrest.Matchers.containsInAnyOrder("Mid Pricewidget", "Pricey Pricewidget")));

        mockMvc.perform(get("/api/listings").param("search", "Pricewidget").param("maxPrice", "60"))
                .andExpect(jsonPath("$.listings[*].title",
                        org.hamcrest.Matchers.containsInAnyOrder("Cheap Pricewidget", "Mid Pricewidget")));

        mockMvc.perform(get("/api/listings").param("search", "Pricewidget")
                        .param("minPrice", "40").param("maxPrice", "60"))
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.contains("Mid Pricewidget")));

        mockMvc.perform(get("/api/listings").param("search", "Pricewidget"))
                .andExpect(jsonPath("$.listings[*].title", org.hamcrest.Matchers.containsInAnyOrder(
                        "Cheap Pricewidget", "Mid Pricewidget", "Pricey Pricewidget")));
    }

    @Test
    void sortingByPriceAscendingAndDescendingOrdersResultsCorrectly() throws Exception {
        listingRepository.save(listing("Mid Sortwidget", "Mid item", new BigDecimal("50.00"),
                Category.OTHER, "mid.jpg", 5L));
        listingRepository.save(listing("Cheap Sortwidget", "Cheap item", new BigDecimal("10.00"),
                Category.OTHER, "cheap.jpg", 5L));
        listingRepository.save(listing("Pricey Sortwidget", "Pricey item", new BigDecimal("100.00"),
                Category.OTHER, "pricey.jpg", 5L));

        mockMvc.perform(get("/api/listings").param("search", "Sortwidget").param("sort", "PRICE_ASC"))
                .andExpect(jsonPath("$.listings[0].title").value("Cheap Sortwidget"))
                .andExpect(jsonPath("$.listings[1].title").value("Mid Sortwidget"))
                .andExpect(jsonPath("$.listings[2].title").value("Pricey Sortwidget"));

        mockMvc.perform(get("/api/listings").param("search", "Sortwidget").param("sort", "PRICE_DESC"))
                .andExpect(jsonPath("$.listings[0].title").value("Pricey Sortwidget"))
                .andExpect(jsonPath("$.listings[1].title").value("Mid Sortwidget"))
                .andExpect(jsonPath("$.listings[2].title").value("Cheap Sortwidget"));
    }

    @Test
    void sortingByNewestOrdersByCreationTimeMostRecentFirstAndIsTheDefault() throws Exception {
        Listing first = listingRepository.save(listing("First Newestwidget", "Posted first", new BigDecimal("10.00"),
                Category.OTHER, "first.jpg", 6L));
        ReflectionTestUtils.setField(first, "createdAt", java.time.Instant.now().minusSeconds(120));
        listingRepository.save(first);
        Listing second = listingRepository.save(listing("Second Newestwidget", "Posted second", new BigDecimal("10.00"),
                Category.OTHER, "second.jpg", 6L));
        ReflectionTestUtils.setField(second, "createdAt", java.time.Instant.now().minusSeconds(60));
        listingRepository.save(second);
        Listing third = listingRepository.save(listing("Third Newestwidget", "Posted third", new BigDecimal("10.00"),
                Category.OTHER, "third.jpg", 6L));
        listingRepository.save(third);

        mockMvc.perform(get("/api/listings").param("search", "Newestwidget"))
                .andExpect(jsonPath("$.listings[0].title").value("Third Newestwidget"))
                .andExpect(jsonPath("$.listings[1].title").value("Second Newestwidget"))
                .andExpect(jsonPath("$.listings[2].title").value("First Newestwidget"));
    }

    @Test
    void paginationReturnsTheRequestedPageAndTheTotalCount() throws Exception {
        for (int i = 1; i <= 5; i++) {
            listingRepository.save(listing("Pagewidget " + i, "Description " + i, new BigDecimal(i + ".00"),
                    Category.OTHER, "item" + i + ".jpg", 7L));
        }

        mockMvc.perform(get("/api/listings").param("search", "Pagewidget")
                        .param("sort", "PRICE_ASC").param("page", "0").param("size", "2"))
                .andExpect(jsonPath("$.listings.length()").value(2))
                .andExpect(jsonPath("$.listings[0].title").value("Pagewidget 1"))
                .andExpect(jsonPath("$.listings[1].title").value("Pagewidget 2"))
                .andExpect(jsonPath("$.totalCount").value(5));

        mockMvc.perform(get("/api/listings").param("search", "Pagewidget")
                        .param("sort", "PRICE_ASC").param("page", "2").param("size", "2"))
                .andExpect(jsonPath("$.listings.length()").value(1))
                .andExpect(jsonPath("$.listings[0].title").value("Pagewidget 5"))
                .andExpect(jsonPath("$.totalCount").value(5));
    }

    @Test
    void pageSizeIsCappedAt48EvenWhenALargerSizeIsRequested() throws Exception {
        for (int i = 1; i <= 50; i++) {
            listingRepository.save(listing("Capwidget " + i, "Description " + i, new BigDecimal("10.00"),
                    Category.OTHER, "capped" + i + ".jpg", 8L));
        }

        mockMvc.perform(get("/api/listings").param("search", "Capwidget").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings.length()").value(48))
                .andExpect(jsonPath("$.totalCount").value(50));
    }

    @Test
    void gettingAnExistingListingByIdReturnsItsFullDetail() throws Exception {
        Listing listing = listingRepository.save(listing("Bike", "Road bike", new BigDecimal("150.00"),
                Category.VEHICLES, "bike.jpg", 1L));

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listing.getId()))
                .andExpect(jsonPath("$.title").value("Bike"))
                .andExpect(jsonPath("$.description").value("Road bike"))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.category").value("VEHICLES"))
                .andExpect(jsonPath("$.photos[0].reference").value("bike.jpg"))
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
                .andExpect(jsonPath("$.photos[0].reference").value(org.hamcrest.Matchers.startsWith("/api/photos/")))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();
        String photoReference = objectMapper.readTree(response).get("photos").get(0).get("reference").asText();

        mockMvc.perform(get("/api/listings"))
                .andExpect(jsonPath("$.listings[?(@.id == " + id + ")].title").value("Vintage Camera"));

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
    void editingWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L));

        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Electric Scooter", "description", "Barely used",
                                "price", "120.00", "category", "VEHICLES"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void editingAnotherUsersListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("quinn");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1));

        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Electric Scooter", "description", "Barely used",
                                "price", "120.00", "category", "VEHICLES")))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void editingASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("rosa");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));
        ReflectionTestUtils.setField(listing, "status", ListingStatus.SOLD);
        listingRepository.save(listing);

        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Electric Scooter", "description", "Barely used",
                                "price", "120.00", "category", "VEHICLES")))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void editingANonexistentListingReturnsNotFound() throws Exception {
        String token = registerAndLogin("sam");

        mockMvc.perform(put("/api/listings/{id}", 999999)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Electric Scooter", "description", "Barely used",
                                "price", "120.00", "category", "VEHICLES")))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void editingWithInvalidPayloadIsRejectedWithValidationErrors() throws Exception {
        Session session = registerAndLoginSession("tina");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "", "description", "", "price", "-5", "category", "VEHICLES")))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void editingYourOwnActiveListingUpdatesItsDetailsAndLeavesThePhotoUnchanged() throws Exception {
        Session session = registerAndLoginSession("uma");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(put("/api/listings/{id}", listing.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Electric Scooter", "description", "Barely used, one owner",
                                "price", "120.00", "category", "OTHER")))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Electric Scooter"))
                .andExpect(jsonPath("$.description").value("Barely used, one owner"))
                .andExpect(jsonPath("$.price").value(120.00))
                .andExpect(jsonPath("$.category").value("OTHER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.photos[0].reference").value("scooter.jpg"));

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(jsonPath("$.title").value("Electric Scooter"))
                .andExpect(jsonPath("$.photos[0].reference").value("scooter.jpg"));
    }

    @Test
    void deletingWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingAnotherUsersListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("irene");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deletingASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("jack");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
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
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(delete("/api/listings/{id}", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listings/{id}", listing.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void buyingWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", 1L));

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void buyingYourOwnListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("liam");
        Listing listing = listingRepository.save(listing("Chair", "Office chair", new BigDecimal("40.00"),
                Category.FURNITURE, "chair.jpg", session.userId()));

        mockMvc.perform(post("/api/listings/{id}/buy", listing.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void buyingAnAlreadySoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("mona");
        Listing listing = listingRepository.save(listing("Chair", "Office chair", new BigDecimal("40.00"),
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
        Listing listing = listingRepository.save(listing("Chair", "Office chair", new BigDecimal("40.00"),
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
        Listing listing = listingRepository.save(listing("Chair", "Office chair", new BigDecimal("40.00"),
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
        listingRepository.save(listing("Guitar", "Acoustic guitar", new BigDecimal("90.00"),
                Category.OTHER, "guitar.jpg", session.userId()));
        listingRepository.save(listing("Drone", "Camera drone", new BigDecimal("250.00"),
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
        Listing bought = listingRepository.save(listing("Tablet", "10-inch tablet", new BigDecimal("200.00"),
                Category.ELECTRONICS, "tablet.jpg", session.userId() + 1));
        ReflectionTestUtils.setField(bought, "status", ListingStatus.SOLD);
        ReflectionTestUtils.setField(bought, "buyerId", session.userId());
        listingRepository.save(bought);
        listingRepository.save(listing("Rug", "Wool rug", new BigDecimal("60.00"),
                Category.HOME_AND_GARDEN, "rug.jpg", session.userId() + 1));

        mockMvc.perform(get("/api/listings/mine/bought")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItem("Tablet")))
                .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Rug"))))
                .andExpect(jsonPath("$[*].buyerId", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.equalTo(session.userId().intValue()))));
    }

    @Test
    void addingAPhotoWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L));

        mockMvc.perform(multipart("/api/listings/{id}/photos", listing.getId())
                        .file(photoPart()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void addingAPhotoToAnotherUsersListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("victor");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1));

        mockMvc.perform(multipart("/api/listings/{id}/photos", listing.getId())
                        .file(photoPart())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void addingAPhotoToASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("wendy");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));
        ReflectionTestUtils.setField(listing, "status", ListingStatus.SOLD);
        listingRepository.save(listing);

        mockMvc.perform(multipart("/api/listings/{id}/photos", listing.getId())
                        .file(photoPart())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void addingAPhotoAppendsItAfterTheExistingOnes() throws Exception {
        Session session = registerAndLoginSession("xena");
        Listing listing = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(multipart("/api/listings/{id}/photos", listing.getId())
                        .file(photoPart())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos.length()").value(2))
                .andExpect(jsonPath("$.photos[0].reference").value("scooter.jpg"))
                .andExpect(jsonPath("$.photos[1].reference", org.hamcrest.Matchers.startsWith("/api/photos/")));
    }

    @Test
    void addingASeventhPhotoIsRejected() throws Exception {
        Session session = registerAndLoginSession("yusuf");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId());
        for (int i = 1; i < 6; i++) {
            listing.addPhoto(new Photo(listing, "extra-" + i + ".jpg", i));
        }
        listingRepository.save(listing);

        mockMvc.perform(multipart("/api/listings/{id}/photos", listing.getId())
                        .file(photoPart())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingAPhotoWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L);
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        Long photoId = saved.getPhotos().get(1).getId();

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), photoId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingAnotherUsersListingPhotoIsRejected() throws Exception {
        Session session = registerAndLoginSession("zara");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1);
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        Long photoId = saved.getPhotos().get(1).getId();

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), photoId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingAPhotoFromASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("amir");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId());
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        ReflectionTestUtils.setField(saved, "status", ListingStatus.SOLD);
        listingRepository.save(saved);
        Long photoId = saved.getPhotos().get(1).getId();

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), photoId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingTheLastRemainingPhotoIsRejected() throws Exception {
        Session session = registerAndLoginSession("bianca");
        Listing saved = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));
        Long photoId = saved.getPhotos().get(0).getId();

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), photoId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingAnUnknownPhotoIdIsRejected() throws Exception {
        Session session = registerAndLoginSession("carlos");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId());
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), 999999)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void removingAPhotoDeletesItAndLeavesTheRemainingOnes() throws Exception {
        Session session = registerAndLoginSession("dalia");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId());
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        Long photoId = saved.getPhotos().get(0).getId();

        mockMvc.perform(delete("/api/listings/{id}/photos/{photoId}", saved.getId(), photoId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos.length()").value(1))
                .andExpect(jsonPath("$.photos[0].reference").value("extra.jpg"));
    }

    @Test
    void reorderingPhotosWithoutAuthenticationIsRejected() throws Exception {
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", 1L);
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        List<Long> ids = saved.getPhotos().stream().map(Photo::getId).toList();

        mockMvc.perform(put("/api/listings/{id}/photos/order", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("photoIds", ids))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void reorderingAnotherUsersListingPhotosIsRejected() throws Exception {
        Session session = registerAndLoginSession("felix");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId() + 1);
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        List<Long> ids = saved.getPhotos().stream().map(Photo::getId).toList();

        mockMvc.perform(put("/api/listings/{id}/photos/order", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("photoIds", ids)))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void reorderingPhotosOnASoldListingIsRejected() throws Exception {
        Session session = registerAndLoginSession("gracie");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId());
        listing.addPhoto(new Photo(listing, "extra.jpg", 1));
        Listing saved = listingRepository.save(listing);
        ReflectionTestUtils.setField(saved, "status", ListingStatus.SOLD);
        listingRepository.save(saved);
        List<Long> ids = saved.getPhotos().stream().map(Photo::getId).toList();

        mockMvc.perform(put("/api/listings/{id}/photos/order", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("photoIds", ids)))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void reorderingWithAMismatchedPhotoIdSetIsRejected() throws Exception {
        Session session = registerAndLoginSession("hank");
        Listing saved = listingRepository.save(listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "scooter.jpg", session.userId()));

        mockMvc.perform(put("/api/listings/{id}/photos/order", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("photoIds", List.of(999999))))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void reorderingPhotosChangesTheirOrderAndTheCoverPhotoInSubsequentResponses() throws Exception {
        Session session = registerAndLoginSession("ivy");
        Listing listing = listing("Scooter", "Electric scooter", new BigDecimal("150.00"),
                Category.VEHICLES, "front.jpg", session.userId());
        listing.addPhoto(new Photo(listing, "back.jpg", 1));
        Listing saved = listingRepository.save(listing);
        Long frontId = saved.getPhotos().get(0).getId();
        Long backId = saved.getPhotos().get(1).getId();

        mockMvc.perform(put("/api/listings/{id}/photos/order", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("photoIds", List.of(backId, frontId))))
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos[0].reference").value("back.jpg"))
                .andExpect(jsonPath("$.photos[1].reference").value("front.jpg"));

        mockMvc.perform(get("/api/listings/{id}", saved.getId()))
                .andExpect(jsonPath("$.photos[0].reference").value("back.jpg"));
    }
}
