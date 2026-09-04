package com.marketplace.backend.favorite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "listingId"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long listingId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Favorite() {
    }

    public Favorite(Long userId, Long listingId) {
        this.userId = userId;
        this.listingId = listingId;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getListingId() {
        return listingId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
