package com.payment.payment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayReconciliationResponse {
        private String entity;
        private int count;
        private List<ReconciliationItem> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ReconciliationItem {
                private Object entityId;
                private String type;
                private Object debit;
                private Object credit;
                private Object amount;
                private String currency;
                private Object fee;
                private Object tax;
                private Object onHold;
                private Object settled;
                private Object createdAt;
                private Object settledAt;
                private Object settlementId;
                private Object postedAt;
                private Object creditType;
                private Object description;
                private Object notes;
                private Object paymentId;
                private Object settlementUtr;
                private Object orderId;
                private Object orderReceipt;
                private Object method;
                private Object cardNetwork;
                private Object cardIssuer;
                private Object cardType;
                private Object disputeId;
        }
}