package com.b2bshop.project.model;

public enum OrderStatus {
    OLUSTURULDU(0, "OLUSTURULDU"),
    ONAYLANDI(1, "ONAYLANDI"),
    TAMAMLANDI(2, "TAMAMLANDI"),
    IPTAL_EDILDI(3, "IPTAL_EDILDI");

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