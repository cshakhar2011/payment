package com.payment.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRazorpayRefundRequest {
        @NotNull(message = "amount is required")
        @Min(value = 100, message = "amount must be at least 100 smallest currency units")
        private Long amount;
        @Pattern(regexp = "normal|optimum", message = "speed must be normal or optimum")
        private String speed;
        private String receipt;
        private Map<String, Object> notes;
}