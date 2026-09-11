package com.payment.payment.entity;

public enum PaymentStatus {
    CREATED,
    PAYMENT_PENDING,
    AUTHORIZED,
    CAPTURED,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED,
    PARTIALLY_REFUNDED
}
