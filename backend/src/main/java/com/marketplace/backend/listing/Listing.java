package com.marketplace.backend.listing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "listing")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "photo_reference")
    private String photoReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(nullable = false)
    private Long ownerId;

    private Long buyerId;

    protected Listing() {
    }

    public Listing(String title, String description, BigDecimal price, Category category,
                   String photoReference, Long ownerId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.photoReference = photoReference;
        this.ownerId = ownerId;
        this.status = ListingStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void update(String title, String description, BigDecimal price, Category category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }
}
