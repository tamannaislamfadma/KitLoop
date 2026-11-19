package com.kitloop.service;

import com.kitloop.dto.EquipmentRequestForm;
import com.kitloop.dto.PaymentInstructions;
import com.kitloop.entity.AdminSetting;
import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentListing;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.ListingType;
import com.kitloop.entity.NotificationType;
import com.kitloop.entity.RequestStatus;
import com.kitloop.entity.Role;
import com.kitloop.exception.RequestFlowException;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.repository.EquipmentRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class EquipmentRequestService {

    private final EquipmentRequestRepository
            equipmentRequestRepository;

    private final AppUserRepository
            appUserRepository;

    private final PaymentScreenshotStorageService
            paymentScreenshotStorageService;

    private final NotificationService
            notificationService;

    private final AdminSettingService
            adminSettingService;

    private final ListingLifecycleService
            listingLifecycleService;

    private final WalletService
            walletService;

    public EquipmentRequestService(
            EquipmentRequestRepository equipmentRequestRepository,
            AppUserRepository appUserRepository,
            PaymentScreenshotStorageService paymentScreenshotStorageService,
            NotificationService notificationService,
            AdminSettingService adminSettingService,
            ListingLifecycleService listingLifecycleService,
            WalletService walletService
    ) {
        this.equipmentRequestRepository =
                equipmentRequestRepository;

        this.appUserRepository =
                appUserRepository;

        this.paymentScreenshotStorageService =
                paymentScreenshotStorageService;

        this.notificationService =
                notificationService;

        this.adminSettingService =
                adminSettingService;

        this.listingLifecycleService =
                listingLifecycleService;

        this.walletService =
                walletService;
    }

    @Transactional
    public PaymentInstructions getPaymentInstructions(
            EquipmentListing listing,
            AppUser requester
    ) {

        validateCanRequest(
                listing,
                requester
        );

        BigDecimal amount =
                determinePayableAmount(
                        listing
                );

        if (listing.getListingType()
                == ListingType.FREE_SHARE) {

            AdminSetting setting =
                    adminSettingService
                            .getCurrentSetting();

            return new PaymentInstructions(
                    amount,
                    "KitLoop Admin",
                    setting.getBkashNumber(),
                    "KitLoop Admin bKash"
            );
        }

        return new PaymentInstructions(
                amount,
                listing
                        .getOwner()
                        .getFullName(),
                listing
                        .getOwner()
                        .getPhone(),
                "Seller bKash"
        );
    }

    @Transactional
    public EquipmentRequest submitRequest(
            EquipmentListing listing,
            AppUser requester,
            EquipmentRequestForm form
    ) {

        validateCanRequest(
                listing,
                requester
        );

        if (equipmentRequestRepository
                .existsByListing_IdAndStatusAndActiveCycleTrue(
                        listing.getId(),
                        RequestStatus.APPROVED
                )) {

            throw new RequestFlowException(
                    "This equipment already has an active approved request."
            );
        }

        if (equipmentRequestRepository
                .existsByListing_IdAndRequester_IdAndStatusInAndActiveCycleTrue(
                        listing.getId(),
                        requester.getId(),
                        List.of(
                                RequestStatus.PENDING,
                                RequestStatus.APPROVED
                        )
                )) {

            throw new RequestFlowException(
                    "You already have an active request for this equipment."
            );
        }

        String transactionId =
                normalizeTransactionId(
                        form.getTransactionId()
                );

        if (equipmentRequestRepository
                .existsByTransactionIdIgnoreCase(
                        transactionId
                )) {

            throw new RequestFlowException(
                    "This transaction ID has already been used."
            );
        }

        PaymentInstructions instructions =
                getPaymentInstructions(
                        listing,
                        requester
                );

        String screenshotFilename =
                paymentScreenshotStorageService
                        .store(
                                form.getPaymentScreenshot()
                        );

        try {

            EquipmentRequest request =
                    new EquipmentRequest();

            request.setListing(listing);
            request.setRequester(requester);
            request.setPayableAmount(
                    instructions.getAmount()
            );
            request.setPaymentReceiverNumber(
                    instructions.getReceiverNumber()
            );
            request.setTransactionId(
                    transactionId
            );
            request.setSenderNumber(
                    form.getSenderNumber()
                            .trim()
            );
            request.setPaymentScreenshotFilename(
                    screenshotFilename
            );
            request.setStatus(
                    RequestStatus.PENDING
            );
            request.setActiveCycle(true);

            EquipmentRequest saved =
                    equipmentRequestRepository
                            .saveAndFlush(
                                    request
                            );

            createSubmissionNotifications(
                    saved
            );

            return saved;

        } catch (RuntimeException exception) {

            paymentScreenshotStorageService
                    .delete(
                            screenshotFilename
                    );

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public EquipmentRequest getRequestForUser(
            Long requestId,
            AppUser user
    ) {

        EquipmentRequest request =
                getRequest(
                        requestId
                );

        boolean requester =
                request.getRequester()
                        .getId()
                        .equals(
                                user.getId()
                        );

        boolean owner =
                request.getListing()
                        .getOwner()
                        .getId()
                        .equals(
                                user.getId()
                        );

        if (!requester
                && !owner) {

            throw new RequestFlowException(
                    "You do not have permission to view this request."
            );
        }

        return request;
    }

    @Transactional(readOnly = true)
    public List<EquipmentRequest> getIncomingRequests(
            Long ownerId
    ) {

        return equipmentRequestRepository
                .findByListing_Owner_IdOrderByCreatedAtDesc(
                        ownerId
                );
    }

    @Transactional(readOnly = true)
    public List<EquipmentRequest> getOutgoingRequests(
            Long requesterId
    ) {

        return equipmentRequestRepository
                .findByRequester_IdOrderByCreatedAtDesc(
                        requesterId
                );
    }

    @Transactional(readOnly = true)
    public List<EquipmentRequest> getFreeShareRequests() {

        return equipmentRequestRepository
                .findByListing_ListingTypeOrderByCreatedAtDesc(
                        ListingType.FREE_SHARE
                );
    }

    @Transactional
    public EquipmentRequest approveByOwner(
            Long requestId,
            AppUser owner
    ) {

        EquipmentRequest request =
                getRequestForUpdate(
                        requestId
                );

        validateOwnerDecision(
                request,
                owner
        );

        return approve(
                request,
                owner
        );
    }

    @Transactional
    public EquipmentRequest rejectByOwner(
            Long requestId,
            AppUser owner
    ) {

        EquipmentRequest request =
                getRequestForUpdate(
                        requestId
                );

        validateOwnerDecision(
                request,
                owner
        );

        return reject(
                request,
                owner
        );
    }

    @Transactional
    public EquipmentRequest approveByAdmin(
            Long requestId,
            AppUser admin
    ) {

        validateAdmin(
                admin
        );

        EquipmentRequest request =
                getRequestForUpdate(
                        requestId
                );

        if (request.getListing()
                .getListingType()
                != ListingType.FREE_SHARE) {

            throw new RequestFlowException(
                    "Only FREE_SHARE requests are approved by the admin."
            );
        }

        return approve(
                request,
                admin
        );
    }

    @Transactional
    public EquipmentRequest rejectByAdmin(
            Long requestId,
            AppUser admin
    ) {

        validateAdmin(
                admin
        );

        EquipmentRequest request =
                getRequestForUpdate(
                        requestId
                );

        if (request.getListing()
                .getListingType()
                != ListingType.FREE_SHARE) {

            throw new RequestFlowException(
                    "Only FREE_SHARE requests are rejected by the admin."
            );
        }

        return reject(
                request,
                admin
        );
    }

    @Transactional(readOnly = true)
    public EquipmentRequest getRequestByScreenshotFilename(
            String filename
    ) {

        return equipmentRequestRepository
                .findByPaymentScreenshotFilename(
                        filename
                )
                .orElseThrow(() ->
                        new RequestFlowException(
                                "Payment screenshot was not found."
                        )
                );
    }

    public boolean canViewPaymentProof(
            EquipmentRequest request,
            AppUser user
    ) {

        if (user.getRole()
                == Role.ADMIN) {
            return true;
        }

        if (request.getRequester()
                .getId()
                .equals(
                        user.getId()
                )) {
            return true;
        }

        return request.getListing()
                .getListingType()
                != ListingType.FREE_SHARE
                && request.getListing()
                .getOwner()
                .getId()
                .equals(
                        user.getId()
                );
    }

    private EquipmentRequest approve(
            EquipmentRequest request,
            AppUser actor
    ) {

        ensurePending(
                request
        );

        if (equipmentRequestRepository
                .existsByListing_IdAndStatusAndActiveCycleTrue(
                        request.getListing()
                                .getId(),
                        RequestStatus.APPROVED
                )) {

            throw new RequestFlowException(
                    "Another active request for this equipment has already been approved."
            );
        }

        request.setStatus(
                RequestStatus.APPROVED
        );

        request.setActiveCycle(true);

        request.setDecidedBy(
                actor
        );

        request.setDecidedAt(
                Instant.now()
        );

        EquipmentRequest saved =
                equipmentRequestRepository
                        .saveAndFlush(
                                request
                        );

        listingLifecycleService
                .applyApprovedRequest(
                        saved
                );

        if (saved.getListing()
                .getListingType()
                == ListingType.FREE_SHARE) {

            walletService
                    .creditFreeShareOwnerOnce(
                            saved
                    );
        }

        notificationService.create(
                saved.getRequester(),
                NotificationType.REQUEST_APPROVED,
                "Request approved",
                "Your request for \""
                        + saved.getListing()
                        .getTitle()
                        + "\" has been approved.",
                saved.getId(),
                saved.getListing()
                        .getId()
        );

        if (saved.getListing()
                .getListingType()
                == ListingType.FREE_SHARE) {

            notificationService.create(
                    saved.getListing()
                            .getOwner(),
                    NotificationType.REQUEST_APPROVED,
                    "FREE_SHARE payment approved",
                    "Admin approved the FREE_SHARE payment for \""
                            + saved.getListing()
                            .getTitle()
                            + "\".",
                    saved.getId(),
                    saved.getListing()
                            .getId()
            );
        }

        rejectCompetingRequests(
                saved,
                actor
        );

        return saved;
    }

    private EquipmentRequest reject(
            EquipmentRequest request,
            AppUser actor
    ) {

        ensurePending(
                request
        );

        request.setStatus(
                RequestStatus.REJECTED
        );

        request.setActiveCycle(false);

        request.setDecidedBy(
                actor
        );

        request.setDecidedAt(
                Instant.now()
        );

        EquipmentRequest saved =
                equipmentRequestRepository
                        .saveAndFlush(
                                request
                        );

        notificationService.create(
                saved.getRequester(),
                NotificationType.REQUEST_REJECTED,
                "Request rejected",
                "Your request for \""
                        + saved.getListing()
                        .getTitle()
                        + "\" has been rejected.",
                saved.getId(),
                saved.getListing()
                        .getId()
        );

        return saved;
    }

    private void rejectCompetingRequests(
            EquipmentRequest approved,
            AppUser actor
    ) {

        List<EquipmentRequest> competing =
                equipmentRequestRepository
                        .findByListing_IdAndStatusAndActiveCycleTrueAndIdNotOrderByCreatedAtDesc(
                                approved.getListing()
                                        .getId(),
                                RequestStatus.PENDING,
                                approved.getId()
                        );

        for (
                EquipmentRequest request
                : competing
        ) {

            request.setStatus(
                    RequestStatus.REJECTED
            );

            request.setActiveCycle(false);

            request.setDecidedBy(
                    actor
            );

            request.setDecidedAt(
                    Instant.now()
            );

            equipmentRequestRepository
                    .save(
                            request
                    );

            notificationService.create(
                    request.getRequester(),
                    NotificationType.REQUEST_REJECTED,
                    "Request closed",
                    "Another request for \""
                            + request.getListing()
                            .getTitle()
                            + "\" was approved.",
                    request.getId(),
                    request.getListing()
                            .getId()
            );
        }
    }

    private void createSubmissionNotifications(
            EquipmentRequest request
    ) {

        EquipmentListing listing =
                request.getListing();

        notificationService.create(
                listing.getOwner(),
                NotificationType.REQUEST_SUBMITTED,
                "New equipment request",
                request.getRequester()
                        .getFullName()
                        + " sent a request for \""
                        + listing.getTitle()
                        + "\".",
                request.getId(),
                listing.getId()
        );

        notificationService.create(
                request.getRequester(),
                NotificationType.REQUEST_SUBMITTED,
                "Request submitted",
                "Your payment proof for \""
                        + listing.getTitle()
                        + "\" was submitted.",
                request.getId(),
                listing.getId()
        );

        if (listing.getListingType()
                == ListingType.FREE_SHARE) {

            notificationService.create(
                    getAdmin(),
                    NotificationType.PAYMENT_SUBMITTED,
                    "New FREE_SHARE token payment",
                    request.getRequester()
                            .getFullName()
                            + " submitted payment proof for \""
                            + listing.getTitle()
                            + "\".",
                    request.getId(),
                    listing.getId()
            );
        }
    }

    private AppUser getAdmin() {

        return appUserRepository
                .findFirstByRole(
                        Role.ADMIN
                )
                .orElseThrow(() ->
                        new RequestFlowException(
                                "KitLoop admin account is not available."
                        )
                );
    }

    private void validateOwnerDecision(
            EquipmentRequest request,
            AppUser owner
    ) {

        if (request.getListing()
                .getListingType()
                == ListingType.FREE_SHARE) {

            throw new RequestFlowException(
                    "FREE_SHARE requests can only be approved or rejected by the admin."
            );
        }

        if (!request.getListing()
                .getOwner()
                .getId()
                .equals(
                        owner.getId()
                )) {

            throw new RequestFlowException(
                    "Only the listing owner can decide this request."
            );
        }
    }

    private void validateCanRequest(
            EquipmentListing listing,
            AppUser requester
    ) {

        if (listing.getOwner()
                .getId()
                .equals(
                        requester.getId()
                )) {

            throw new RequestFlowException(
                    "You cannot request your own equipment."
            );
        }

        if (!isAvailable(
                listing
        )) {

            throw new RequestFlowException(
                    "This equipment is not currently available for requests."
            );
        }
    }

    private boolean isAvailable(
            EquipmentListing listing
    ) {

        if (listing.getListingType()
                == ListingType.RENT) {

            return listing.getStatus()
                    == ListingStatus.OPEN_FOR_RENT;
        }

        return listing.getStatus()
                == ListingStatus.ACTIVE;
    }

    private BigDecimal determinePayableAmount(
            EquipmentListing listing
    ) {

        BigDecimal amount;

        if (listing.getListingType()
                == ListingType.FREE_SHARE) {

            amount =
                    listing.getRefundableDeposit();

        } else if (
                listing.getListingType()
                        == ListingType.RENT
        ) {

            amount =
                    listing.getRentPrice();

        } else {

            amount =
                    listing.getSalePrice();
        }

        if (amount == null
                || amount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new RequestFlowException(
                    "This listing does not have a valid payable amount."
            );
        }

        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private EquipmentRequest getRequest(
            Long requestId
    ) {

        return equipmentRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RequestFlowException(
                                "Equipment request was not found."
                        )
                );
    }

    private EquipmentRequest getRequestForUpdate(
            Long requestId
    ) {

        return equipmentRequestRepository
                .findByIdForUpdate(
                        requestId
                )
                .orElseThrow(() ->
                        new RequestFlowException(
                                "Equipment request was not found."
                        )
                );
    }

    private void validateAdmin(
            AppUser admin
    ) {

        if (admin.getRole()
                != Role.ADMIN) {

            throw new RequestFlowException(
                    "Administrator permission is required."
            );
        }
    }

    private void ensurePending(
            EquipmentRequest request
    ) {

        if (request.getStatus()
                != RequestStatus.PENDING) {

            throw new RequestFlowException(
                    "This request has already been decided."
            );
        }
    }

    private String normalizeTransactionId(
            String transactionId
    ) {

        return transactionId
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }
}