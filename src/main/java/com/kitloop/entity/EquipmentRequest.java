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
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "equipment_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_equipment_requests_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
public class EquipmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "listing_id",
            nullable = false
    )
    private EquipmentListing listing;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "requester_id",
            nullable = false
    )
    private AppUser requester;

    @Column(
            name = "payable_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal payableAmount;

    @Column(
            name = "payment_receiver_number",
            nullable = false,
            length = 20
    )
    private String paymentReceiverNumber;

    @Column(
            name = "transaction_id",
            nullable = false,
            length = 40
    )
    private String transactionId;

    @Column(
            name = "sender_number",
            nullable = false,
            length = 20
    )
    private String senderNumber;

    @Column(
            name = "payment_screenshot_filename",
            nullable = false,
            length = 120
    )
    private String paymentScreenshotFilename;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private RequestStatus status;

    @Column(
            name = "active_cycle",
            nullable = false
    )
    @ColumnDefault("1")
    private boolean activeCycle = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "decided_by_id")
    private AppUser decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

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

    public EquipmentRequest() {
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

    public EquipmentListing getListing() {
        return listing;
    }

    public void setListing(
            EquipmentListing listing
    ) {
        this.listing = listing;
    }

    public AppUser getRequester() {
        return requester;
    }

    public void setRequester(
            AppUser requester
    ) {
        this.requester = requester;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(
            BigDecimal payableAmount
    ) {
        this.payableAmount = payableAmount;
    }

    public String getPaymentReceiverNumber() {
        return paymentReceiverNumber;
    }

    public void setPaymentReceiverNumber(
            String paymentReceiverNumber
    ) {
        this.paymentReceiverNumber =
                paymentReceiverNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(
            String transactionId
    ) {
        this.transactionId = transactionId;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(
            String senderNumber
    ) {
        this.senderNumber = senderNumber;
    }

    public String getPaymentScreenshotFilename() {
        return paymentScreenshotFilename;
    }

    public void setPaymentScreenshotFilename(
            String paymentScreenshotFilename
    ) {
        this.paymentScreenshotFilename =
                paymentScreenshotFilename;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(
            RequestStatus status
    ) {
        this.status = status;
    }

    public boolean isActiveCycle() {
        return activeCycle;
    }

    public void setActiveCycle(
            boolean activeCycle
    ) {
        this.activeCycle = activeCycle;
    }

    public AppUser getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(
            AppUser decidedBy
    ) {
        this.decidedBy = decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(
            Instant decidedAt
    ) {
        this.decidedAt = decidedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}