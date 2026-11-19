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

import java.time.Instant;

@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "recipient_id",
            nullable = false
    )
    private AppUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private NotificationType type;

    @Column(
            nullable = false,
            length = 120
    )
    private String title;

    @Column(
            nullable = false,
            length = 500
    )
    private String message;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "listing_id")
    private Long listingId;

    @Column(
            name = "is_read",
            nullable = false
    )
    private boolean read;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public UserNotification() {
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getRecipient() {
        return recipient;
    }

    public void setRecipient(
            AppUser recipient
    ) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(
            NotificationType type
    ) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message = message;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(
            Long requestId
    ) {
        this.requestId = requestId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(
            Long listingId
    ) {
        this.listingId = listingId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(
            boolean read
    ) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}