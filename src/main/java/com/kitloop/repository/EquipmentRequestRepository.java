package com.kitloop.repository;

import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.ListingType;
import com.kitloop.entity.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipmentRequestRepository
        extends JpaRepository<EquipmentRequest, Long> {

    boolean existsByTransactionIdIgnoreCase(
            String transactionId
    );

    boolean existsByListing_IdAndStatusAndActiveCycleTrue(
            Long listingId,
            RequestStatus status
    );

    boolean existsByListing_IdAndRequester_IdAndStatusInAndActiveCycleTrue(
            Long listingId,
            Long requesterId,
            Collection<RequestStatus> statuses
    );

    long countByRequester_IdAndStatusAndActiveCycleTrue(
            Long requesterId,
            RequestStatus status
    );

    long countByListing_ListingTypeAndStatus(
            ListingType listingType,
            RequestStatus status
    );

    List<EquipmentRequest>
    findByRequester_IdOrderByCreatedAtDesc(
            Long requesterId
    );

    List<EquipmentRequest>
    findByListing_Owner_IdOrderByCreatedAtDesc(
            Long ownerId
    );

    List<EquipmentRequest>
    findByListing_ListingTypeOrderByCreatedAtDesc(
            ListingType listingType
    );

    List<EquipmentRequest>
    findByListing_IdAndStatusAndActiveCycleTrueAndIdNotOrderByCreatedAtDesc(
            Long listingId,
            RequestStatus status,
            Long requestId
    );

    Optional<EquipmentRequest>
    findFirstByListing_IdAndStatusAndActiveCycleTrueOrderByDecidedAtDesc(
            Long listingId,
            RequestStatus status
    );

    Optional<EquipmentRequest>
    findByPaymentScreenshotFilename(
            String filename
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT request
            FROM EquipmentRequest request
            WHERE request.id = :requestId
            """)
    Optional<EquipmentRequest>
    findByIdForUpdate(
            @Param("requestId")
            Long requestId
    );
}