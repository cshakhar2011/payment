package com.payment.payment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpaySettlementResponse {
        private String entity;
        private int count;
        private List<SettlementItem> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SettlementItem {
                private String id;
                private String entity;
                private Object amount;
                private String status;
                private Object fees;
                private Object tax;
                private String utr;
                private Object createdAt;
        }
}