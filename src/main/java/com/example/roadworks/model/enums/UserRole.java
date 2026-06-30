package com.example.roadworks.model.enums;

public enum UserRole {
    ADMIN("Administrator"),
    SUPERVISOR("Supervisor"),
    VIEWER("Viewer");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
