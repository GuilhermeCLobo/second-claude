package com.marketplace.backend.listing;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

final class ListingSpecifications {

    private ListingSpecifications() {
    }

    static Specification<Listing> statusIs(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<Listing> categoryIs(Category category) {
        if (category == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    static Specification<Listing> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern));
    }

    static Specification<Listing> priceAtLeast(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    static Specification<Listing> priceAtMost(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    static Specification<Listing> and(Specification<Listing>... specifications) {
        Specification<Listing> combined = Specification.where(null);
        for (Specification<Listing> specification : specifications) {
            if (specification != null) {
                combined = combined.and(specification);
            }
        }
        return combined;
    }
}
