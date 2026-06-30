package com.example.roadworks.model.enums;

public enum AlertType {
    EXPIRING_7_DAYS("Expires in 7 days"),
    EXPIRING_3_DAYS("Expires in 3 days!"),
    EXPIRED("Period exceeded!"),
    SIGNS_NOT_REMOVED("Temporary signs not removed!");

    private final String label;

    AlertType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
