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

import java.time.Instant;

@Entity
@Table(
        name = "trust_score_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trust_score_event_key",
                        columnNames = "event_key"
                )
        }
)
public class TrustScoreHistory {

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
            name = "event_type",
            nullable = false,
            length = 60
    )
    private TrustScoreEventType eventType;

    @Column(
            nullable = false
    )
    private int points;

    @Column(
            name = "score_after",
            nullable = false
    )
    private int scoreAfter;

    @Column(
            name = "event_key",
            nullable = false,
            length = 140
    )
    private String eventKey;

    @Column(name = "listing_id")
    private Long listingId;

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

    public TrustScoreHistory() {
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

    public TrustScoreEventType getEventType() {
        return eventType;
    }

    public void setEventType(
            TrustScoreEventType eventType
    ) {
        this.eventType = eventType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(
            int points
    ) {
        this.points = points;
    }

    public int getScoreAfter() {
        return scoreAfter;
    }

    public void setScoreAfter(
            int scoreAfter
    ) {
        this.scoreAfter = scoreAfter;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(
            String eventKey
    ) {
        this.eventKey = eventKey;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(
            Long listingId
    ) {
        this.listingId = listingId;
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