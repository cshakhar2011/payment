package com.payment.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWebhookRequest {
        private String url;
        @Email(message = "alertEmail must be a valid email")
        private String alertEmail;
        @NotEmpty(message = "events must not be empty")
        private List<String> events;
}