package com.kitloop.service;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.EquipmentRequest;
import com.kitloop.entity.ListingType;
import com.kitloop.entity.NotificationType;
import com.kitloop.entity.RequestStatus;
import com.kitloop.entity.WalletTransaction;
import com.kitloop.entity.WalletTransactionType;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WalletService {

    private final AppUserRepository
            appUserRepository;

    private final WalletTransactionRepository
            walletTransactionRepository;

    private final NotificationService
            notificationService;

    public WalletService(
            AppUserRepository appUserRepository,
            WalletTransactionRepository walletTransactionRepository,
            NotificationService notificationService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.walletTransactionRepository =
                walletTransactionRepository;

        this.notificationService =
                notificationService;
    }

    @Transactional
    public void creditFreeShareOwnerOnce(
            EquipmentRequest request
    ) {

        if (request.getStatus()
                != RequestStatus.APPROVED) {

            throw new IllegalStateException(
                    "Only approved requests can credit a wallet."
            );
        }

        if (request
                .getListing()
                .getListingType()
                != ListingType.FREE_SHARE) {

            return;
        }

        String eventKey =
                "FREE_SHARE_APPROVAL:"
                        + request.getId();

        if (walletTransactionRepository
                .existsByEventKey(
                        eventKey
                )) {
            return;
        }

        AppUser owner =
                appUserRepository
                        .findByIdForUpdate(
                                request
                                        .getListing()
                                        .getOwner()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Listing owner no longer exists."
                                )
                        );

        if (walletTransactionRepository
                .existsByEventKey(
                        eventKey
                )) {
            return;
        }

        BigDecimal currentBalance =
                owner.getWalletBalance()
                        == null
                        ? BigDecimal.ZERO
                        : owner.getWalletBalance();

        BigDecimal amount =
                request.getPayableAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal newBalance =
                currentBalance
                        .add(amount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        owner.setWalletBalance(
                newBalance
        );

        appUserRepository.save(
                owner
        );

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setUser(
                owner
        );

        transaction.setType(
                WalletTransactionType
                        .FREE_SHARE_TOKEN_CREDIT
        );

        transaction.setAmount(
                amount
        );

        transaction.setBalanceAfter(
                newBalance
        );

        transaction.setEventKey(
                eventKey
        );

        transaction.setRequestId(
                request.getId()
        );

        transaction.setDescription(
                "FREE_SHARE token payment approved for \""
                        + request
                        .getListing()
                        .getTitle()
                        + "\"."
        );

        walletTransactionRepository
                .saveAndFlush(
                        transaction
                );

        notificationService.create(
                owner,
                NotificationType.WALLET_CREDITED,
                "Wallet credited",
                "৳"
                        + amount
                        + " was credited to your KitLoop wallet for \""
                        + request
                        .getListing()
                        .getTitle()
                        + "\".",
                request.getId(),
                request
                        .getListing()
                        .getId()
        );
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction>
    getRecentHistory(
            Long userId
    ) {

        return walletTransactionRepository
                .findTop5ByUser_IdOrderByCreatedAtDesc(
                        userId
                );
    }

    @Transactional(readOnly = true)
    public BigDecimal
    getApprovedTokenTotal() {

        return walletTransactionRepository
                .findByTypeOrderByCreatedAtDesc(
                        WalletTransactionType
                                .FREE_SHARE_TOKEN_CREDIT
                )
                .stream()
                .map(
                        WalletTransaction::getAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}