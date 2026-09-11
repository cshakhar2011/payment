package com.payment.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
        @NotBlank(message = "razorpay_order_id is required")
        private String razorpayOrderId;
        @NotBlank(message = "razorpay_payment_id is required")
        private String razorpayPaymentId;
        @NotBlank(message = "razorpay_signature is required")
        private String razorpaySignature;
}