package com.payment.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.exception.PaymentProviderException;
import com.payment.payment.dto.RazorpayWebhookResponse;
import com.payment.payment.dto.UpdateWebhookRequest;
import com.payment.payment.entity.Webhook;
import com.payment.payment.repository.WebhookRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class WebhookService {

    private final RazorpayService razorpayService;
    private final WebhookRepository webhookRepository;
    private final ObjectMapper objectMapper;

    public WebhookService(RazorpayService razorpayService, WebhookRepository webhookRepository,
            ObjectMapper objectMapper) {
        this.razorpayService = razorpayService;
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
    }

    public List<RazorpayWebhookResponse> listWebhooks(String accountId) {
        validateAccountId(accountId);
        try {
            return razorpayService.listWebhooks(accountId).stream()
                    .map(this::persist)
                    .toList();
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to list webhooks from provider", ex);
        }
    }

    public RazorpayWebhookResponse getWebhook(String accountId, String webhookId) {
        validateAccountId(accountId);
        if (webhookId == null || webhookId.isBlank()) {
            throw new IllegalArgumentException("webhookId is required");
        }
        try {
            return persist(razorpayService.getWebhook(accountId, webhookId));
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to fetch webhook from provider", ex);
        }
    }

    public RazorpayWebhookResponse updateWebhook(String accountId, String webhookId, UpdateWebhookRequest request) {
        validateAccountId(accountId);
        if (webhookId == null || webhookId.isBlank()) {
            throw new IllegalArgumentException("webhookId is required");
        }
        try {
            return persist(razorpayService.updateWebhook(accountId, webhookId, request));
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to update webhook with provider", ex);
        }
    }

    public List<RazorpayWebhookResponse> deleteWebhook(String accountId, String webhookId) {
        validateAccountId(accountId);
        if (webhookId == null || webhookId.isBlank()) {
            throw new IllegalArgumentException("webhookId is required");
        }
        try {
            List<RazorpayWebhookResponse> response = razorpayService.deleteWebhook(accountId, webhookId);
            webhookRepository.findByProviderWebhookId(webhookId).ifPresent(webhookRepository::delete);
            return response;
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to delete webhook from provider", ex);
        }
    }

    private RazorpayWebhookResponse persist(RazorpayWebhookResponse response) {
        try {
            Webhook webhook = webhookRepository.findByProviderWebhookId(response.getId())
                    .orElseGet(Webhook::new);
            webhook.setProviderWebhookId(response.getId());
            webhook.setService(response.getService());
            webhook.setOwnerId(response.getOwnerId());
            webhook.setOwnerType(response.getOwnerType());
            webhook.setUrl(response.getUrl());
            webhook.setAlertEmail(response.getAlertEmail());
            webhook.setSecretExists(response.getSecretExists() instanceof Boolean value ? value : null);
            webhook.setActive(response.getActive() instanceof Boolean value ? value : null);
            webhook.setEventsJson(objectMapper.writeValueAsString(response.getEvents()));
            webhook.setDisabledAt(response.getDisabledAt() instanceof Number value ? value.longValue() : null);
            webhookRepository.save(webhook);
            return response;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize webhook events", ex);
        }
    }

    private void validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId is required");
        }
    }
}