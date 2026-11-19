package com.kitloop.service;

import com.kitloop.entity.AppUser;
import com.kitloop.entity.TrustScoreEventType;
import com.kitloop.entity.TrustScoreHistory;
import com.kitloop.repository.AppUserRepository;
import com.kitloop.repository.TrustScoreHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrustScoreService {

    private final AppUserRepository
            appUserRepository;

    private final TrustScoreHistoryRepository
            trustScoreHistoryRepository;

    public TrustScoreService(
            AppUserRepository appUserRepository,
            TrustScoreHistoryRepository trustScoreHistoryRepository
    ) {
        this.appUserRepository =
                appUserRepository;

        this.trustScoreHistoryRepository =
                trustScoreHistoryRepository;
    }

    @Transactional
    public void awardOnce(
            Long userId,
            int points,
            TrustScoreEventType eventType,
            Long listingId,
            Long requestId,
            String eventKey,
            String description
    ) {

        if (trustScoreHistoryRepository
                .existsByEventKey(
                        eventKey
                )) {
            return;
        }

        AppUser user =
                appUserRepository
                        .findByIdForUpdate(
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Trust score user no longer exists."
                                )
                        );

        if (trustScoreHistoryRepository
                .existsByEventKey(
                        eventKey
                )) {
            return;
        }

        int scoreAfter =
                user.getTrustScore()
                        + points;

        user.setTrustScore(
                scoreAfter
        );

        appUserRepository.save(
                user
        );

        TrustScoreHistory history =
                new TrustScoreHistory();

        history.setUser(
                user
        );

        history.setEventType(
                eventType
        );

        history.setPoints(
                points
        );

        history.setScoreAfter(
                scoreAfter
        );

        history.setListingId(
                listingId
        );

        history.setRequestId(
                requestId
        );

        history.setEventKey(
                eventKey
        );

        history.setDescription(
                description
        );

        trustScoreHistoryRepository
                .saveAndFlush(
                        history
                );
    }

    @Transactional(readOnly = true)
    public List<TrustScoreHistory>
    getRecentHistory(
            Long userId
    ) {

        return trustScoreHistoryRepository
                .findTop5ByUser_IdOrderByCreatedAtDesc(
                        userId
                );
    }
}