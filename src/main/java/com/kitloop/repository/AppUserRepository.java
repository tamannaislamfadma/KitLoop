package com.kitloop.repository;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser>
    findByEmailIgnoreCase(
            String email
    );

    Optional<AppUser>
    findFirstByRole(
            Role role
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    long countByRole(
            Role role
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT user
            FROM AppUser user
            WHERE user.id = :userId
            """)
    Optional<AppUser> findByIdForUpdate(
            @Param("userId")
            Long userId
    );
}