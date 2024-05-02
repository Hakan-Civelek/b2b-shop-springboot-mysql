package com.b2bshop.project.model;

public enum OrderStatus {
    ORDER_PLACED("Sipariş Verildi"),
    ORDER_CONFIRMED("Onaylandı"),
    ORDER_COMPLETED("Tamamlandı");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}