package com.payment.payment.controller;

import com.payment.payment.dto.RazorpayWebhookResponse;
import com.payment.payment.dto.UpdateWebhookRequest;
import com.payment.payment.service.WebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webhooks")
@Tag(name = "Webhooks", description = "Payment provider webhook endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "List webhooks", description = "Lists webhook configurations for a Razorpay account.")
    public List<RazorpayWebhookResponse> listWebhooks(@PathVariable String accountId) {
        return webhookService.listWebhooks(accountId);
    }

    @GetMapping("/{accountId}/{webhookId}")
    @Operation(summary = "Get a webhook", description = "Fetches one Razorpay webhook configuration.")
    public RazorpayWebhookResponse getWebhook(@PathVariable String accountId, @PathVariable String webhookId) {
        return webhookService.getWebhook(accountId, webhookId);
    }

    @PatchMapping("/{accountId}/{webhookId}")
    @Operation(summary = "Update a webhook", description = "Updates a Razorpay webhook URL and event subscriptions.")
    public RazorpayWebhookResponse updateWebhook(
            @PathVariable String accountId,
            @PathVariable String webhookId,
            @Valid @RequestBody UpdateWebhookRequest request) {
        return webhookService.updateWebhook(accountId, webhookId, request);
    }

    @DeleteMapping("/{accountId}/{webhookId}")
    @Operation(summary = "Delete a webhook", description = "Deletes a Razorpay webhook configuration.")
    public List<RazorpayWebhookResponse> deleteWebhook(
            @PathVariable String accountId,
            @PathVariable String webhookId) {
        return webhookService.deleteWebhook(accountId, webhookId);
    }

    @PostMapping("/razorpay")
    @Operation(summary = "Receive a Razorpay webhook", description = "Accepts payment provider events for asynchronous processing.")
    public ResponseEntity<Void> razorpayWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {

        return ResponseEntity.ok().build();
    }
}
