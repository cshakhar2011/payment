package com.payment.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayQrCodeResponse {
        private String id;
        private String entity;
        private Object createdAt;
        private String name;
        private String usage;
        private String type;
        private String imageUrl;
        private Object paymentAmount;
        private String status;
        private String description;
        private Object fixedAmount;
        private Object paymentsAmountReceived;
        private Object paymentsCountReceived;
        private Object notes;
        private Object customerId;
        private Object closeBy;
        private Object closedAt;
        private String closeReason;
}