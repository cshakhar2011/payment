package com.payment.payment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayWebhookResponse {
        private String id;
        private Object createdAt;
        private String service;
        private String ownerId;
        private String ownerType;
        private Object context;
        private String url;
        private String alertEmail;
        private Object secretExists;
        private String entity;
        private Object active;
        private List<String> events;
        private Object disabledAt;
}