package com.kitloop.repository;

import com.kitloop.entity.EquipmentCategory;
import com.kitloop.entity.EquipmentCondition;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.ListingType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipmentListingRepository
        extends JpaRepository<EquipmentListing, Long> {

    List<EquipmentListing>
    findByOwner_IdOrderByCreatedAtDesc(
            Long ownerId
    );

    long countByOwner_Id(
            Long ownerId
    );

    long countByOwner_IdAndStatusIn(
            Long ownerId,
            Collection<ListingStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT listing
            FROM EquipmentListing listing
            WHERE listing.id = :listingId
            """)
    Optional<EquipmentListing>
    findByIdForUpdate(
            @Param("listingId")
            Long listingId
    );

    @Query("""
            SELECT listing
            FROM EquipmentListing listing
            WHERE
                (
                    :searchText IS NULL
                    OR LOWER(listing.title)
                        LIKE LOWER(CONCAT('%', :searchText, '%'))
                    OR LOWER(listing.description)
                        LIKE LOWER(CONCAT('%', :searchText, '%'))
                )
                AND (
                    :category IS NULL
                    OR listing.category = :category
                )
                AND (
                    :listingType IS NULL
                    OR listing.listingType = :listingType
                )
                AND (
                    :condition IS NULL
                    OR listing.condition = :condition
                )
                AND (
                    :status IS NULL
                    OR listing.status = :status
                )
            ORDER BY listing.createdAt DESC
            """)
    List<EquipmentListing> searchMarketplace(
            @Param("searchText")
            String searchText,
            @Param("category")
            EquipmentCategory category,
            @Param("listingType")
            ListingType listingType,
            @Param("condition")
            EquipmentCondition condition,
            @Param("status")
            ListingStatus status
    );
}