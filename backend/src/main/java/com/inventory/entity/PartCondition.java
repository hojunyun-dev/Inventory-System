package com.inventory.entity;

public enum PartCondition {
    NEW("신품"),
    USED("중고품"),
    REFURBISHED("리퍼비시"),
    REMANUFACTURED("재제조");

    private final String description;

    PartCondition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}