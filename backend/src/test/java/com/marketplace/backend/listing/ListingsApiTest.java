package com.marketplace.backend.listing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListingsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

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
}
