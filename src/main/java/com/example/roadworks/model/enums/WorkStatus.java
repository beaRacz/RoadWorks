package com.example.roadworks.model.enums;

public enum WorkStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    EXTENDED("Extended"),
    CANCELLED("Cancelled");

    private final String label;

    WorkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
