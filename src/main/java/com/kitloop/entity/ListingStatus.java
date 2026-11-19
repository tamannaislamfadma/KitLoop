package com.kitloop.entity;

public enum ListingStatus {

    ACTIVE("Active"),
    OPEN_FOR_RENT("Open for Rent"),
    RENTED("Rented"),
    SOLD("Sold"),
    FREE_SHARED("Free Shared"),
    RETURNED_AFTER_SHARE("Returned After Share");

    private final String label;

    ListingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}