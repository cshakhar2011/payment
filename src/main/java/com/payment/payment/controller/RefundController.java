package com.payment.payment.controller;

import com.payment.payment.dto.CreateRazorpayRefundRequest;
import com.payment.payment.dto.RazorpayRefundResponse;
import com.payment.payment.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refunds")
@Tag(name = "Refunds", description = "Payment refund operations")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "Get a refund", description = "Fetches one Razorpay refund.")
    public RazorpayRefundResponse getRefund(@PathVariable String refundId) {
        return refundService.getRefund(refundId);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Creates a full or partial Razorpay payment refund.")
    public RazorpayRefundResponse createRefund(
            @PathVariable String paymentId,
            @RequestHeader("X-Refund-Idempotency") String idempotencyKey,
            @Valid @RequestBody CreateRazorpayRefundRequest request) {
        return refundService.createRefund(paymentId, idempotencyKey, request);
    }
}