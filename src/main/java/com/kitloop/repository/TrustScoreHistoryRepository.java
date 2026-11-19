package com.kitloop.repository;

import com.kitloop.entity.TrustScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrustScoreHistoryRepository
        extends JpaRepository<TrustScoreHistory, Long> {

    boolean existsByEventKey(
            String eventKey
    );

    List<TrustScoreHistory>
    findTop5ByUser_IdOrderByCreatedAtDesc(
            Long userId
    );
}