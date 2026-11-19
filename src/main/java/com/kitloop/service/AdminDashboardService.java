package com.kitloop.service;

import com.kitloop.dto.AdminDashboardStats;
import com.kitloop.entity.ListingType;
import com.kitloop.entity.RequestStatus;
import com.kitloop.entity.Role;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.repository.EquipmentListingRepository;
import com.kitloop.repository.EquipmentRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final AppUserRepository
            appUserRepository;

    private final EquipmentListingRepository
            equipmentListingRepository;

    private final EquipmentRequestRepository
            equipmentRequestRepository;

    private final WalletService
            walletService;

    public AdminDashboardService(
            AppUserRepository appUserRepository,
            EquipmentListingRepository equipmentListingRepository,
            EquipmentRequestRepository equipmentRequestRepository,
            WalletService walletService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.equipmentListingRepository =
                equipmentListingRepository;

        this.equipmentRequestRepository =
                equipmentRequestRepository;

        this.walletService =
                walletService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats getStats() {

        long users =
                appUserRepository
                        .countByRole(
                                Role.USER
                        );

        long listings =
                equipmentListingRepository
                        .count();

        long pendingTokens =
                equipmentRequestRepository
                        .countByListing_ListingTypeAndStatus(
                                ListingType.FREE_SHARE,
                                RequestStatus.PENDING
                        );

        return new AdminDashboardStats(
                users,
                listings,
                pendingTokens,
                walletService
                        .getApprovedTokenTotal()
        );
    }
}