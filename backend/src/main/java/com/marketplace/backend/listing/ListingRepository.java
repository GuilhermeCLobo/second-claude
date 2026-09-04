package com.marketplace.backend.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByCategory(Category category);

    List<Listing> findByOwnerId(Long ownerId);

    List<Listing> findByBuyerId(Long buyerId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Listing l SET l.status = com.marketplace.backend.listing.ListingStatus.SOLD, l.buyerId = :buyerId " +
            "WHERE l.id = :id AND l.status = com.marketplace.backend.listing.ListingStatus.ACTIVE")
    int markSoldIfActive(@Param("id") Long id, @Param("buyerId") Long buyerId);
}
