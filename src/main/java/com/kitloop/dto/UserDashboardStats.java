package com.kitloop.dto;

public class UserDashboardStats {

    private final long myListings;
    private final long activeListings;
    private final long pendingRequests;
    private final long unreadNotifications;

    public UserDashboardStats(
            long myListings,
            long activeListings,
            long pendingRequests,
            long unreadNotifications
    ) {
        this.myListings = myListings;
        this.activeListings = activeListings;
        this.pendingRequests = pendingRequests;
        this.unreadNotifications = unreadNotifications;
    }

    public long getMyListings() {
        return myListings;
    }

    public long getActiveListings() {
        return activeListings;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public long getUnreadNotifications() {
        return unreadNotifications;
    }
}