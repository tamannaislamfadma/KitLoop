package com.kitloop.service;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.NotificationType;
import com.kitloop.entity.UserNotification;
import com.kitloop.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final UserNotificationRepository
            userNotificationRepository;

    public NotificationService(
            UserNotificationRepository userNotificationRepository
    ) {
        this.userNotificationRepository =
                userNotificationRepository;
    }

    @Transactional
    public void create(
            AppUser recipient,
            NotificationType type,
            String title,
            String message,
            Long requestId,
            Long listingId
    ) {

        UserNotification notification =
                new UserNotification();

        notification.setRecipient(
                recipient
        );

        notification.setType(
                type
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setRequestId(
                requestId
        );

        notification.setListingId(
                listingId
        );

        notification.setRead(false);

        userNotificationRepository.save(
                notification
        );
    }

    @Transactional(readOnly = true)
    public List<UserNotification>
    getNotifications(
            Long userId
    ) {

        return userNotificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(
                        userId
                );
    }

    @Transactional
    public void markAllRead(
            Long userId
    ) {

        List<UserNotification> unread =
                userNotificationRepository
                        .findByRecipient_IdAndReadFalse(
                                userId
                        );

        for (
                UserNotification notification
                : unread
        ) {
            notification.setRead(true);
        }

        userNotificationRepository
                .saveAll(unread);
    }
}