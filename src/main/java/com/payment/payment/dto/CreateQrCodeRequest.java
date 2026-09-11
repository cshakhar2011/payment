package com.payment.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQrCodeRequest {
        @NotBlank(message = "type is required")
        private String type;
        @NotBlank(message = "name is required")
        private String name;
        @NotBlank(message = "usage is required")
        private String usage;
        @NotNull(message = "fixedAmount is required")
        private Boolean fixedAmount;
        @Min(value = 100, message = "paymentAmount must be at least 100 smallest currency units")
        private Long paymentAmount;
        private String description;
        private String customerId;
        private Long closeBy;
        private Map<String, Object> notes;
}