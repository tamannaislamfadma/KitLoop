package com.kitloop.dto;

import java.math.BigDecimal;

public class AdminDashboardStats {

    private final long users;
    private final long listings;
    private final long pendingTokenPayments;
    private final BigDecimal approvedTokenTotal;

    public AdminDashboardStats(
            long users,
            long listings,
            long pendingTokenPayments,
            BigDecimal approvedTokenTotal
    ) {
        this.users = users;
        this.listings = listings;
        this.pendingTokenPayments =
                pendingTokenPayments;
        this.approvedTokenTotal =
                approvedTokenTotal;
    }

    public long getUsers() {
        return users;
    }

    public long getListings() {
        return listings;
    }

    public long getPendingTokenPayments() {
        return pendingTokenPayments;
    }

    public BigDecimal getApprovedTokenTotal() {
        return approvedTokenTotal;
    }
}