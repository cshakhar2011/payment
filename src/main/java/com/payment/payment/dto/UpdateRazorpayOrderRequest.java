package com.payment.payment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRazorpayOrderRequest {
        @NotNull(message = "notes are required")
        private Map<String, Object> notes;
}