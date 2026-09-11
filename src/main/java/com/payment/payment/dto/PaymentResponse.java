package com.payment.payment.dto;

import com.payment.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentReference;
    private String orderReference;
    private String providerPaymentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private OffsetDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getOrder().getOrderReference(),
                payment.getProviderPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getPaymentMethod(),
                payment.getCreatedAt());
    }
}