package com.kitloop.repository;

import com.kitloop.entity.WalletTransaction;
import com.kitloop.entity.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    boolean existsByEventKey(
            String eventKey
    );

    List<WalletTransaction>
    findTop5ByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    List<WalletTransaction>
    findByTypeOrderByCreatedAtDesc(
            WalletTransactionType type
    );
}