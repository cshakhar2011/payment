package com.payment.payment.controller;

import com.payment.payment.dto.MakePaymentRequest;
import com.payment.payment.dto.PaymentOrderResponse;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.service.MakePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments/orders")
@Tag(name = "Payments", description = "Payment authorization operations")
@SecurityRequirement(name = "bearerAuth")
public class MakePaymentController {

    private final MakePaymentService makePaymentService;

    public MakePaymentController(MakePaymentService makePaymentService) {
        this.makePaymentService = makePaymentService;
    }

    @PostMapping("/{orderReference}/pay")
    @Operation(summary = "Make a payment", description = "Verifies and records a payment for an existing order.")
    public PaymentResponse makePayment(@PathVariable String orderReference,
            @Valid @RequestBody MakePaymentRequest request) {
        return makePaymentService.makePayment(orderReference, request);
    }

    @PostMapping("/{orderReference}/retry")
    @Operation(summary = "Retry a payment", description = "Creates a new provider order for a payment retry.")
    public PaymentOrderResponse retryPayment(@PathVariable String orderReference) {
        return makePaymentService.retryPayment(orderReference);
    }
}