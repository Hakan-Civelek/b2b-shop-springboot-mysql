package com.b2bshop.project.model;

public enum OrderStatus {
    CREATED(0, "CREATED"),
    APPROVED(1, "APPROVED"),
    COMPLETED(2, "COMPLETED"),
    CANCELED(3, "CANCELED");

    private final int id;
    private final String status;

    OrderStatus(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public static OrderStatus getById(int id) {
        for (OrderStatus status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus id: " + id);
    }
}