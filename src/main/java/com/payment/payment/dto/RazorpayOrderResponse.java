package com.payment.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
        private String id;
        private BigDecimal amount;
        private BigDecimal amountPaid;
        private BigDecimal amountDue;
        private String currency;
        private String receipt;
        private String status;
        private Long attempts;
        private String notes;
        private OffsetDateTime createdAt;
        private Object payments;
}