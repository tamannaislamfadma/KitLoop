package com.kitloop.entity;

public enum ListingPaymentStatus {

    NOT_APPLICABLE("Not Applicable"),
    PENDING("Pending"),
    PAID("Paid");

    private final String label;

    ListingPaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}