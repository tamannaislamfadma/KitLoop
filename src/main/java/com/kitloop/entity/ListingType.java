package com.kitloop.entity;

public enum ListingType {

    FREE_SHARE("Free Share"),
    RENT("Rent"),
    SALE("Sale");

    private final String label;

    ListingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}