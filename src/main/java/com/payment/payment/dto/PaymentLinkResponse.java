package com.payment.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
        private Object upiLink;
        private Object acceptPartial;
        private Object amount;
        private Object amountPaid;
        private Object callbackMethod;
        private Object callbackUrl;
        private Object cancelledAt;
        private Object createdAt;
        private Object currency;
        private Object customer;
        private Object description;
        private Object expireBy;
        private Object expiredAt;
        private Object firstMinPartialAmount;
        private Object id;
        private Object notes;
        private Object notification;
        private Object payments;
        private Object referenceId;
        private Object reminderEnable;
        private Object reminders;
        private Object shortUrl;
        private Object status;
        private Object updatedAt;
        private Object userId;
}