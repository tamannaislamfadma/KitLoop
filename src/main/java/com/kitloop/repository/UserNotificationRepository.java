package com.kitloop.repository;

import com.kitloop.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository
        extends JpaRepository<UserNotification, Long> {

    List<UserNotification>
    findByRecipient_IdOrderByCreatedAtDesc(
            Long recipientId
    );

    List<UserNotification>
    findByRecipient_IdAndReadFalse(
            Long recipientId
    );

    long countByRecipient_IdAndReadFalse(
            Long recipientId
    );
}