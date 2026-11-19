package com.kitloop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "equipment_listings")
public class EquipmentListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "owner_id",
            nullable = false
    )
    private AppUser owner;

    @Column(
            nullable = false,
            length = 120
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private EquipmentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "equipment_condition",
            nullable = false,
            length = 30
    )
    private EquipmentCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "listing_type",
            nullable = false,
            length = 30
    )
    private ListingType listingType;

    @Column(
            name = "rent_price",
            precision = 19,
            scale = 2
    )
    private BigDecimal rentPrice;

    @Column(
            name = "sale_price",
            precision = 19,
            scale = 2
    )
    private BigDecimal salePrice;

    @Column(
            name = "refundable_deposit",
            precision = 19,
            scale = 2
    )
    private BigDecimal refundableDeposit;

    @Column(
            nullable = false,
            length = 2000
    )
    private String description;

    @Column(
            name = "image_filename",
            nullable = false,
            length = 120
    )
    private String imageFilename;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private ListingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    private ListingPaymentStatus paymentStatus;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public EquipmentListing() {
    }

    @PrePersist
    public void onCreate() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public void setCategory(
            EquipmentCategory category
    ) {
        this.category = category;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }

    public void setCondition(
            EquipmentCondition condition
    ) {
        this.condition = condition;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(
            ListingType listingType
    ) {
        this.listingType = listingType;
    }

    public BigDecimal getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(
            BigDecimal rentPrice
    ) {
        this.rentPrice = rentPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(
            BigDecimal salePrice
    ) {
        this.salePrice = salePrice;
    }

    public BigDecimal getRefundableDeposit() {
        return refundableDeposit;
    }

    public void setRefundableDeposit(
            BigDecimal refundableDeposit
    ) {
        this.refundableDeposit = refundableDeposit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(
            String imageFilename
    ) {
        this.imageFilename = imageFilename;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(
            ListingStatus status
    ) {
        this.status = status;
    }

    public ListingPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            ListingPaymentStatus paymentStatus
    ) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}