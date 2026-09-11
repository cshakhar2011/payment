package com.payment.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayPaymentResponse {
        private String id;
        private String entity;
        private Object amount;
        private String currency;
        private String status;
        private String orderId;
        private Object invoiceId;
        private Object terminalId;
        private Object international;
        private String method;
        private Object amountRefunded;
        private Object refundStatus;
        private Object captured;
        private Object description;
        private Object cardId;
        private Object card;
        private Object bank;
        private Object wallet;
        private Object vpa;
        private Object email;
        private Object contact;
        private Object notes;
        private Object fee;
        private Object tax;
        private Object errorCode;
        private Object errorDescription;
        private Object errorSource;
        private Object errorStep;
        private Object errorReason;
        private Object acquirerData;
        private Object createdAt;
        private Object upi;
        private Object customerId;
        private Object tokenId;
        private Object provider;
        private Object reward;
}