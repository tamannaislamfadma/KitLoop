package com.kitloop.service;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.ListingPaymentStatus;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.ListingType;
import com.kitloop.entity.NotificationType;
import com.kitloop.entity.RequestStatus;
import com.kitloop.entity.TrustScoreEventType;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.EquipmentListingRepository;
import com.kitloop.repository.EquipmentRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingLifecycleService {

    private final EquipmentListingRepository
            equipmentListingRepository;

    private final EquipmentRequestRepository
            equipmentRequestRepository;

    private final TrustScoreService
            trustScoreService;

    private final NotificationService
            notificationService;

    public ListingLifecycleService(
            EquipmentListingRepository equipmentListingRepository,
            EquipmentRequestRepository equipmentRequestRepository,
            TrustScoreService trustScoreService,
            NotificationService notificationService
    ) {
        this.equipmentListingRepository =
                equipmentListingRepository;

        this.equipmentRequestRepository =
                equipmentRequestRepository;

        this.trustScoreService =
                trustScoreService;

        this.notificationService =
                notificationService;
    }

    @Transactional
    public void applyApprovedRequest(
            EquipmentRequest request
    ) {

        EquipmentListing listing =
                equipmentListingRepository
                        .findByIdForUpdate(
                                request
                                        .getListing()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new RequestFlowException(
                                        "Listing was not found."
                                )
                        );

        Long ownerId =
                listing
                        .getOwner()
                        .getId();

        Long requestId =
                request.getId();

        Long listingId =
                listing.getId();

        if (listing.getListingType()
                == ListingType.SALE) {

            if (listing.getStatus()
                    != ListingStatus.ACTIVE) {

                throw new RequestFlowException(
                        "This sale listing is no longer active."
                );
            }

            listing.setStatus(
                    ListingStatus.SOLD
            );

            listing.setPaymentStatus(
                    ListingPaymentStatus.PAID
            );

            equipmentListingRepository.save(
                    listing
            );

            trustScoreService.awardOnce(
                    ownerId,
                    5,
                    TrustScoreEventType.OWNER_SOLD,
                    listingId,
                    requestId,
                    "SOLD:"
                            + requestId,
                    "Equipment sold: "
                            + listing.getTitle()
            );

            return;
        }

        if (listing.getListingType()
                == ListingType.RENT) {

            if (listing.getStatus()
                    != ListingStatus.OPEN_FOR_RENT) {

                throw new RequestFlowException(
                        "This rental listing is no longer open for rent."
                );
            }

            listing.setStatus(
                    ListingStatus.RENTED
            );

            listing.setPaymentStatus(
                    ListingPaymentStatus.PAID
            );

            equipmentListingRepository.save(
                    listing
            );

            trustScoreService.awardOnce(
                    ownerId,
                    3,
                    TrustScoreEventType.OWNER_RENTED,
                    listingId,
                    requestId,
                    "RENTED:"
                            + requestId,
                    "Equipment rented: "
                            + listing.getTitle()
            );

            return;
        }

        if (listing.getListingType()
                == ListingType.FREE_SHARE) {

            if (listing.getStatus()
                    != ListingStatus.ACTIVE) {

                throw new RequestFlowException(
                        "This FREE_SHARE listing is no longer active."
                );
            }

            listing.setStatus(
                    ListingStatus.FREE_SHARED
            );

            listing.setPaymentStatus(
                    ListingPaymentStatus.PAID
            );

            equipmentListingRepository.save(
                    listing
            );

            trustScoreService.awardOnce(
                    ownerId,
                    4,
                    TrustScoreEventType.OWNER_FREE_SHARED,
                    listingId,
                    requestId,
                    "FREE_SHARED:"
                            + requestId,
                    "Equipment free-shared: "
                            + listing.getTitle()
            );
        }
    }

    @Transactional
    public void reopenRental(
            Long listingId,
            AppUser owner
    ) {

        EquipmentListing listing =
                equipmentListingRepository
                        .findByIdForUpdate(
                                listingId
                        )
                        .orElseThrow(() ->
                                new RequestFlowException(
                                        "Listing was not found."
                                )
                        );

        validateOwner(
                listing,
                owner
        );

        if (listing.getListingType()
                != ListingType.RENT) {

            throw new RequestFlowException(
                    "Only RENT listings can be reopened."
            );
        }

        if (listing.getStatus()
                != ListingStatus.RENTED) {

            throw new RequestFlowException(
                    "This listing is not currently RENTED."
            );
        }

        EquipmentRequest request =
                getActiveApprovedRequest(
                        listingId
                );

        listing.setStatus(
                ListingStatus.OPEN_FOR_RENT
        );

        listing.setPaymentStatus(
                ListingPaymentStatus.NOT_APPLICABLE
        );

        equipmentListingRepository.save(
                listing
        );

        request.setActiveCycle(false);

        equipmentRequestRepository.save(
                request
        );

        trustScoreService.awardOnce(
                owner.getId(),
                4,
                TrustScoreEventType
                        .OWNER_RENT_RETURNED_TO_OPEN,
                listingId,
                request.getId(),
                "RENT_RETURNED:"
                        + request.getId(),
                "Rental completed and reopened: "
                        + listing.getTitle()
        );

        notificationService.create(
                request.getRequester(),
                NotificationType.LISTING_STATUS_UPDATED,
                "Rental completed",
                "\""
                        + listing.getTitle()
                        + "\" has been marked returned by the owner.",
                request.getId(),
                listingId
        );

        notificationService.create(
                owner,
                NotificationType.LISTING_STATUS_UPDATED,
                "Rental reopened",
                "\""
                        + listing.getTitle()
                        + "\" is open for rent again.",
                request.getId(),
                listingId
        );
    }

    @Transactional
    public void markFreeShareReturned(
            Long listingId,
            AppUser owner
    ) {

        EquipmentListing listing =
                equipmentListingRepository
                        .findByIdForUpdate(
                                listingId
                        )
                        .orElseThrow(() ->
                                new RequestFlowException(
                                        "Listing was not found."
                                )
                        );

        validateOwner(
                listing,
                owner
        );

        if (listing.getListingType()
                != ListingType.FREE_SHARE) {

            throw new RequestFlowException(
                    "Only FREE_SHARE listings use this return action."
            );
        }

        if (listing.getStatus()
                != ListingStatus.FREE_SHARED) {

            throw new RequestFlowException(
                    "This equipment is not currently FREE_SHARED."
            );
        }

        EquipmentRequest request =
                getActiveApprovedRequest(
                        listingId
                );

        listing.setStatus(
                ListingStatus.RETURNED_AFTER_SHARE
        );

        equipmentListingRepository.save(
                listing
        );

        request.setActiveCycle(false);

        equipmentRequestRepository.save(
                request
        );

        trustScoreService.awardOnce(
                request
                        .getRequester()
                        .getId(),
                5,
                TrustScoreEventType
                        .BORROWER_RETURNED_SHARED_ITEM,
                listingId,
                request.getId(),
                "FREE_SHARE_BORROWER_RETURN:"
                        + request.getId(),
                "Successfully returned shared equipment: "
                        + listing.getTitle()
        );

        trustScoreService.awardOnce(
                owner.getId(),
                6,
                TrustScoreEventType
                        .OWNER_FREE_SHARE_RETURNED,
                listingId,
                request.getId(),
                "FREE_SHARE_OWNER_RETURN:"
                        + request.getId(),
                "Shared equipment successfully returned: "
                        + listing.getTitle()
        );

        notificationService.create(
                request.getRequester(),
                NotificationType.LISTING_STATUS_UPDATED,
                "Return confirmed",
                "The owner confirmed your successful return of \""
                        + listing.getTitle()
                        + "\". You earned 5 trust points.",
                request.getId(),
                listingId
        );

        notificationService.create(
                owner,
                NotificationType.LISTING_STATUS_UPDATED,
                "Shared equipment returned",
                "\""
                        + listing.getTitle()
                        + "\" is now RETURNED_AFTER_SHARE.",
                request.getId(),
                listingId
        );
    }

    private EquipmentRequest getActiveApprovedRequest(
            Long listingId
    ) {

        return equipmentRequestRepository
                .findFirstByListing_IdAndStatusAndActiveCycleTrueOrderByDecidedAtDesc(
                        listingId,
                        RequestStatus.APPROVED
                )
                .orElseThrow(() ->
                        new RequestFlowException(
                                "No active approved request exists for this listing."
                        )
                );
    }

    private void validateOwner(
            EquipmentListing listing,
            AppUser owner
    ) {

        if (!listing
                .getOwner()
                .getId()
                .equals(
                        owner.getId()
                )) {

            throw new RequestFlowException(
                    "Only the listing owner can perform this action."
            );
        }
    }
}