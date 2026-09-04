package com.marketplace.backend.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndListingId(Long userId, Long listingId);

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByListingId(Long listingId);

    @Query("SELECT f.listingId FROM Favorite f WHERE f.userId = :userId AND f.listingId IN :listingIds")
    Set<Long> findFavoritedListingIds(@Param("userId") Long userId, @Param("listingIds") Collection<Long> listingIds);
}
