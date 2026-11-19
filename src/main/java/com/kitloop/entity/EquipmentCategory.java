package com.kitloop.entity;

public enum EquipmentCategory {

    ELECTRONICS("Electronics"),
    COMPUTER_ACCESSORIES("Computer & Accessories"),
    CAMERA_MEDIA("Camera & Media"),
    SPORTS("Sports"),
    LAB_EQUIPMENT("Lab Equipment"),
    TOOLS("Tools"),
    BOOKS_STUDY("Books & Study Materials"),
    MUSICAL_INSTRUMENTS("Musical Instruments"),
    OTHER("Other");

    private final String label;

    EquipmentCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}