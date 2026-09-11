package com.payment.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {
        private Long id;
        private String orderReference;
//        private UUID customerId;
        private Long customerId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String provider;
        private String providerOrderId;
}
