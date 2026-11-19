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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "wallet_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallet_transaction_event_key",
                        columnNames = "event_key"
                )
        }
)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private WalletTransactionType type;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balanceAfter;

    @Column(
            name = "event_key",
            nullable = false,
            length = 120
    )
    private String eventKey;

    @Column(name = "request_id")
    private Long requestId;

    @Column(
            nullable = false,
            length = 300
    )
    private String description;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public WalletTransaction() {
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(
            AppUser user
    ) {
        this.user = user;
    }

    public WalletTransactionType getType() {
        return type;
    }

    public void setType(
            WalletTransactionType type
    ) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount
    ) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(
            BigDecimal balanceAfter
    ) {
        this.balanceAfter = balanceAfter;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(
            String eventKey
    ) {
        this.eventKey = eventKey;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(
            Long requestId
    ) {
        this.requestId = requestId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}