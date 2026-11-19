package com.kitloop.service;

import com.kitloop.dto.UserDashboardStats;
import com.kitloop.entity.ListingStatus;
import com.kitloop.entity.RequestStatus;
import com.kitloop.repository.EquipmentListingRepository;
import com.kitloop.repository.EquipmentRequestRepository;
import com.kitloop.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDashboardService {

    private final EquipmentListingRepository
            equipmentListingRepository;

    private final EquipmentRequestRepository
            equipmentRequestRepository;

    private final UserNotificationRepository
            userNotificationRepository;

    public UserDashboardService(
            EquipmentListingRepository equipmentListingRepository,
            EquipmentRequestRepository equipmentRequestRepository,
            UserNotificationRepository userNotificationRepository
    ) {
        this.equipmentListingRepository =
                equipmentListingRepository;

        this.equipmentRequestRepository =
                equipmentRequestRepository;

        this.userNotificationRepository =
                userNotificationRepository;
    }

    @Transactional(readOnly = true)
    public UserDashboardStats getStatsForUser(
            Long userId
    ) {

        long myListings =
                equipmentListingRepository
                        .countByOwner_Id(
                                userId
                        );

        long activeListings =
                equipmentListingRepository
                        .countByOwner_IdAndStatusIn(
                                userId,
                                List.of(
                                        ListingStatus.ACTIVE,
                                        ListingStatus.OPEN_FOR_RENT
                                )
                        );

        long pendingRequests =
                equipmentRequestRepository
                        .countByRequester_IdAndStatusAndActiveCycleTrue(
                                userId,
                                RequestStatus.PENDING
                        );

        long unreadNotifications =
                userNotificationRepository
                        .countByRecipient_IdAndReadFalse(
                                userId
                        );

        return new UserDashboardStats(
                myListings,
                activeListings,
                pendingRequests,
                unreadNotifications
        );
    }
}