package com.payment.payment.controller;

import com.payment.payment.dto.*;
import com.payment.payment.service.PaymentService;
import com.payment.config.PaymentProviderProperties;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment order operations")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentProviderProperties providerProperties;

    public PaymentController(PaymentService paymentService, PaymentProviderProperties providerProperties) {
        this.paymentService = paymentService;
        this.providerProperties = providerProperties;
    }

    @GetMapping("/config")
    @Operation(summary = "Get checkout configuration", description = "Returns the public provider configuration required by the browser checkout.")
    public Map<String, String> getCheckoutConfig() {
        return Map.of("provider", providerProperties.name(), "keyId",
                providerProperties.keyId() == null ? "" : providerProperties.keyId());
    }

    @PostMapping("/payment-links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a payment link", description = "Creates a Razorpay payment link.")
    public PaymentLinkResponse createPaymentLink(@Valid @RequestBody CreatePaymentLinkRequest request) {
        return paymentService.createPaymentLink(request);
    }

    @PostMapping("/orders/{orderReference}/payment-link")
    @Operation(summary = "Create a hosted payment link", description = "Creates a Razorpay hosted payment link for an existing order.")
    public PaymentLinkResponse createPaymentLinkForOrder(@PathVariable String orderReference) {
        return paymentService.createPaymentLinkForOrder(orderReference);
    }

    @GetMapping("/qr-codes/{qrCodeId}")
    @Operation(summary = "Get a QR code", description = "Fetches one Razorpay QR code.")
    public RazorpayQrCodeResponse getQrCode(@PathVariable String qrCodeId) {
        return paymentService.getQrCode(qrCodeId);
    }

    @PostMapping("/qr-codes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a QR code", description = "Creates a Razorpay UPI QR code.")
    public RazorpayQrCodeResponse createQrCode(@Valid @RequestBody CreateQrCodeRequest request) {
        return paymentService.createQrCode(request);
    }

    @GetMapping("/orders/{orderId}/payments")
    @Operation(summary = "List order payments", description = "Lists payments made against a Razorpay order.")
    public List<RazorpayPaymentResponse> listOrderPayments(@PathVariable String orderId) {
        return paymentService.listOrderPayments(orderId);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get a payment", description = "Fetches one payment from the configured payment provider.")
    public RazorpayPaymentResponse getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @PostMapping("/{paymentId}/capture")
    @Operation(summary = "Capture a payment", description = "Captures an authorized Razorpay payment.")
    public RazorpayPaymentResponse capturePayment(
            @PathVariable String paymentId,
            @Valid @RequestBody CapturePaymentRequest request) {
        return paymentService.capturePayment(paymentId, request);
    }

    @GetMapping("/downtimes")
    @Operation(summary = "List payment downtimes", description = "Lists active and scheduled payment method downtimes.")
    public RazorpayDowntimeResponse listDowntimes() {
        return paymentService.listDowntimes();
    }

    @GetMapping("/downtimes/{downtimeId}")
    @Operation(summary = "Get a payment downtime", description = "Fetches one payment downtime record.")
    public RazorpayDowntimeResponse.RazorpayDowntimeItem getDowntime(@PathVariable String downtimeId) {
        return paymentService.getDowntime(downtimeId);
    }

    @GetMapping("/settlements")
    @Operation(summary = "List settlements", description = "Lists Razorpay settlements.")
    public RazorpaySettlementResponse listSettlements(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "0") int skip) {
        return paymentService.listSettlements(count, skip);
    }

    @GetMapping("/settlements/{settlementId}")
    @Operation(summary = "Get a settlement", description = "Fetches one Razorpay settlement.")
    public RazorpaySettlementResponse.SettlementItem getSettlement(@PathVariable String settlementId) {
        return paymentService.getSettlement(settlementId);
    }

    @GetMapping("/settlements/recon/combined")
    @Operation(summary = "Get settlement reconciliation", description = "Gets combined settlement reconciliation records for a date.")
    public RazorpayReconciliationResponse getCombinedReconciliation(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        return paymentService.getCombinedReconciliation(year, month, day);
    }

    @PostMapping("/razorpay/verify")
    @Operation(summary = "Verify a Razorpay payment", description = "Validates the Razorpay signature on the backend.")
    public PaymentOrderResponse verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request);
    }
}
