package com.payment.payment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayDowntimeResponse {
        private String entity;
        private int count;
        private List<RazorpayDowntimeItem> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RazorpayDowntimeItem {
                private String id;
                private String entity;
                private String method;
                private Object begin;
                private Object end;
                private String status;
                private Object scheduled;
                private String severity;
                private Object instrument;
                private Object createdAt;
                private Object updatedAt;
        }
}