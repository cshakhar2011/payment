package com.payment.payment.entity;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    AUTHORIZED,
    CAPTURED,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
