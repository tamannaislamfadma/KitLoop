package com.kitloop.entity;

public enum EquipmentCondition {

    LIKE_NEW("Like New"),
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair");

    private final String label;

    EquipmentCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}