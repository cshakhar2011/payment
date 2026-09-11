package com.payment.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayRefundResponse {
        private String id;
        private String entity;
        private Object amount;
        private String currency;
        private String paymentId;
        private Object notes;
        private Object receipt;
        private Object acquirerData;
        private Object createdAt;
        private Object batchId;
        private String status;
        private String speedProcessed;
        private String speedRequested;
}