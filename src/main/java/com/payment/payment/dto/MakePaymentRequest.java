package com.payment.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentRequest {
        @NotBlank(message = "razorpayPaymentId is required")
        private String razorpayPaymentId;
        @NotBlank(message = "razorpaySignature is required")
        private String razorpaySignature;
        private String paymentMethod;
}