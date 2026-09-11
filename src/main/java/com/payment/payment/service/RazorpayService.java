package com.payment.payment.service;

import com.payment.config.PaymentProviderProperties;
import com.payment.config.RazorpayClientProvider;
import com.payment.payment.dto.*;
import com.payment.payment.entity.ProviderData;
import com.payment.payment.mapper.RazorpayMapper;
import com.payment.payment.repository.ProviderDataRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RazorpayService {

    private final PaymentProviderProperties properties;
    private final RazorpayClientProvider razorpayClientProvider;
    private final ProviderDataRepository providerDataRepository;
    private final ObjectMapper objectMapper;

    public RazorpayService(PaymentProviderProperties properties, RazorpayClientProvider razorpayClientProvider,
            ProviderDataRepository providerDataRepository, ObjectMapper objectMapper) {
        this.properties = properties;
        this.razorpayClientProvider = razorpayClientProvider;
        this.providerDataRepository = providerDataRepository;
        this.objectMapper = objectMapper;
    }

    public ProviderOrderResponse createOrder(BigDecimal amount, String currency, String receipt) throws Exception {

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();

        JSONObject request = new JSONObject();
        long amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).longValueExact();
        request.put("amount", amountInSmallestUnit);
        request.put("currency", currency);
        request.put("receipt", receipt);
        JSONObject notes = new JSONObject();
        notes.put("source", "payment-service");
        request.put("notes", notes);
        Order order = razorpayClient.orders.create(request);
        ProviderOrderResponse response = new ProviderOrderResponse(order.get("id"), order.get("amount"),
                order.get("currency"),
                order.get("status"), order.get("receipt"));
        persist("order", response.orderId(), response);
        return response;
    }

    public List<RazorpayWebhookResponse> listWebhooks(String accountId) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        return razorpayClient.webhook.fetchAll(accountId).stream()
                .map(RazorpayMapper::toWebhookResponse)
                .collect(Collectors.toList());
    }

    public RazorpayWebhookResponse getWebhook(String accountId, String webhookId) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        return RazorpayMapper.toWebhookResponse(razorpayClient.webhook.fetch(accountId, webhookId));
    }

    public RazorpayWebhookResponse updateWebhook(String accountId, String webhookId, UpdateWebhookRequest request)
            throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        JSONObject payload = new JSONObject();
        if (request.getUrl() != null && !request.getUrl().isBlank()) {
            payload.put("url", request.getUrl());
        }
        if (request.getAlertEmail() != null && !request.getAlertEmail().isBlank()) {
            payload.put("alert_email", request.getAlertEmail());
        }
        payload.put("events", request.getEvents());
        return RazorpayMapper.toWebhookResponse(razorpayClient.webhook.edit(accountId, webhookId, payload));
    }

    public List<RazorpayWebhookResponse> deleteWebhook(String accountId, String webhookId) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        return razorpayClient.webhook.delete(accountId, webhookId).stream()
                .map(RazorpayMapper::toWebhookResponse)
                .collect(Collectors.toList());
    }

    public PaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request) throws RazorpayException {
        validateCredentials();
        JSONObject payload = new JSONObject();
        payload.put("amount", request.getAmount());
        payload.put("currency", request.getCurrency().toUpperCase());
        if (request.getUpiLink() != null) {
            payload.put("upi_link", request.getUpiLink());
        }
        payload.put("accept_partial", request.isAcceptPartial());
        payload.put("description", request.getDescription());

        if (request.getFirstMinPartialAmount() != null) {
            payload.put("first_min_partial_amount", request.getFirstMinPartialAmount());
        }
        if (request.getExpireBy() != null) {
            payload.put("expire_by", request.getExpireBy());
        }
        if (request.getReferenceId() != null && !request.getReferenceId().isBlank()) {
            payload.put("reference_id", request.getReferenceId());
        }
        CreatePaymentLinkRequest.Customer customer = request.getCustomer();
        JSONObject customerPayload = new JSONObject();
        if (customer.getName() != null) {
            customerPayload.put("name", customer.getName());
        }
        if (customer.getContact() != null) {
            customerPayload.put("contact", customer.getContact());
        }
        if (customer.getEmail() != null) {
            customerPayload.put("email", customer.getEmail());
        }
        if (!customerPayload.isEmpty()) {
            payload.put("customer", customerPayload);
        }

        if (request.getNotification() != null) {
            JSONObject notify = new JSONObject();
            if (request.getNotification().getSms() != null) {
                notify.put("sms", request.getNotification().getSms());
            }
            if (request.getNotification().getEmail() != null) {
                notify.put("email", request.getNotification().getEmail());
            }
            payload.put("notify", notify);
        }
        payload.put("reminder_enable", request.isReminderEnable());
        if (request.getNotes() != null) {
            payload.put("notes", new JSONObject(request.getNotes()));
        }
        if (request.getCallbackUrl() != null && !request.getCallbackUrl().isBlank()) {
            payload.put("callback_url", request.getCallbackUrl());
        }
        if (request.getCallbackMethod() != null && !request.getCallbackMethod().isBlank()) {
            payload.put("callback_method", request.getCallbackMethod());
        }

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        PaymentLinkResponse response = RazorpayMapper.toPaymentLinkResponse(razorpayClient.paymentLink.create(payload));
        persist("payment_link", String.valueOf(response.getId()), response);
        return response;
    }

    public RazorpayQrCodeResponse getQrCode(String qrCodeId) throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        com.razorpay.QrCode qrCode = razorpayClient.qrCode.fetch(qrCodeId);
        RazorpayQrCodeResponse response = RazorpayMapper.toQrCodeResponse(qrCode);
        persist("qr_code", response.getId(), response);
        return response;
    }

    public RazorpayQrCodeResponse createQrCode(CreateQrCodeRequest request) throws RazorpayException {
        validateCredentials();
        JSONObject payload = new JSONObject();
        payload.put("type", request.getType());
        payload.put("name", request.getName());
        payload.put("usage", request.getUsage());
        payload.put("fixed_amount", request.getFixedAmount());
        if (request.getPaymentAmount() != null) {
            payload.put("payment_amount", request.getPaymentAmount());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            payload.put("description", request.getDescription());
        }
        if (request.getCustomerId() != null && !request.getCustomerId().isBlank()) {
            payload.put("customer_id", request.getCustomerId());
        }
        if (request.getCloseBy() != null) {
            payload.put("close_by", request.getCloseBy());
        }
        if (request.getNotes() != null) {
            payload.put("notes", new JSONObject(request.getNotes()));
        }

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpayQrCodeResponse response = RazorpayMapper.toQrCodeResponse(razorpayClient.qrCode.create(payload));
        persist("qr_code", response.getId(), response);
        return response;
    }

    public List<RazorpayOrderResponse> listOrders(int count, int skip) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }
        if (skip < 0) {
            throw new IllegalArgumentException("skip must not be negative");
        }

        JSONObject query = new JSONObject();
        query.put("count", count);
        query.put("skip", skip);
        query.put("expand[]", "payments.card");

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        return razorpayClient.orders.fetchAll(query).stream()
                .map(RazorpayMapper::toOrderResponse)
                .peek(response -> persist("order", response.getId(), response))
                .collect(Collectors.toList());
    }

    public RazorpayOrderResponse getOrder(String orderId) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpayOrderResponse response = RazorpayMapper.toOrderResponse(razorpayClient.orders.fetch(orderId));
        persist("order", response.getId(), response);
        return response;
    }

    public RazorpayOrderResponse updateOrder(String orderId, Map<String, Object> notes) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }

        JSONObject request = new JSONObject();
        request.put("notes", new JSONObject(notes));
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpayOrderResponse response = RazorpayMapper.toOrderResponse(razorpayClient.orders.edit(orderId, request));
        persist("order", response.getId(), response);
        return response;
    }

    public List<RazorpayPaymentResponse> listOrderPayments(String orderId) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        return razorpayClient.orders.fetchPayments(orderId).stream()
                .map(RazorpayMapper::toPaymentResponse)
                .peek(response -> persist("payment", response.getId(), response))
                .collect(Collectors.toList());
    }

    public RazorpayPaymentResponse getPayment(String paymentId) throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }

        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        JSONObject query = new JSONObject();
        query.put("expand[]", "card");
        RazorpayPaymentResponse response = RazorpayMapper
                .toPaymentResponse(razorpayClient.payments.expandedDetails(paymentId, query));
        persist("payment", response.getId(), response);
        return response;
    }

    public RazorpayPaymentResponse capturePayment(String paymentId, Long amount, String currency)
            throws RazorpayException {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }

        JSONObject request = new JSONObject();
        request.put("amount", amount);
        request.put("currency", currency.toUpperCase());
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpayPaymentResponse response = RazorpayMapper
                .toPaymentResponse(razorpayClient.payments.capture(paymentId, request));
        persist("payment", response.getId(), response);
        return response;
    }

    public RazorpayDowntimeResponse listDowntimes() throws RazorpayException {
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        List<RazorpayDowntimeResponse.RazorpayDowntimeItem> items = razorpayClient.payments.fetchPaymentDowntime()
                .stream()
                .map(RazorpayMapper::toDowntimeItem)
                .peek(item -> persist("downtime", item.getId(), item))
                .collect(Collectors.toList());
        return new RazorpayDowntimeResponse("collection", items.size(), items);
    }

    public RazorpayDowntimeResponse.RazorpayDowntimeItem getDowntime(String downtimeId) throws RazorpayException {
        validateCredentials();
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpayDowntimeResponse.RazorpayDowntimeItem item = RazorpayMapper.toDowntimeItem(
                razorpayClient.payments.fetchPaymentDowntimeById(downtimeId));
        persist("downtime", item.getId(), item);
        return item;
    }

    public RazorpaySettlementResponse listSettlements(int count, int skip) throws RazorpayException {
        validateCredentials();
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }
        if (skip < 0) {
            throw new IllegalArgumentException("skip must not be negative");
        }

        JSONObject query = new JSONObject();
        query.put("count", count);
        query.put("skip", skip);
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        List<RazorpaySettlementResponse.SettlementItem> items = razorpayClient.settlement.fetchAll(query)
                .stream()
                .map(RazorpayMapper::toSettlementItem)
                .peek(item -> persist("settlement", item.getId(), item))
                .collect(Collectors.toList());
        return new RazorpaySettlementResponse("collection", items.size(), items);
    }

    public RazorpaySettlementResponse.SettlementItem getSettlement(String settlementId) throws RazorpayException {
        validateCredentials();
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        RazorpaySettlementResponse.SettlementItem item = RazorpayMapper.toSettlementItem(
                razorpayClient.settlement.fetch(settlementId));
        persist("settlement", item.getId(), item);
        return item;
    }

    public RazorpayReconciliationResponse getCombinedReconciliation(int year, int month, int day)
            throws RazorpayException {
        validateCredentials();
        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("year, month, and day must form a valid date", ex);
        }

        JSONObject query = new JSONObject();
        query.put("year", year);
        query.put("month", month);
        query.put("day", day);
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        List<RazorpayReconciliationResponse.ReconciliationItem> items = razorpayClient.settlement.reports(query)
                .stream()
                .map(RazorpayMapper::toReconciliationItem)
                .peek(item -> persist("reconciliation", String.valueOf(item.getEntityId()), item))
                .collect(Collectors.toList());
        return new RazorpayReconciliationResponse("collection", items.size(), items);
    }

    public RazorpayRefundResponse getRefund(String refundId) throws RazorpayException {
        validateCredentials();
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        com.razorpay.Refund refund = razorpayClient.payments.fetchRefund(refundId);
        RazorpayRefundResponse response = RazorpayMapper.toRefundResponse(refund);
        persist("refund", response.getId(), response);
        return response;
    }

    public RazorpayRefundResponse refundPayment(String paymentId, String idempotencyKey, Long amount, String speed,
            String receipt, Map<String, Object> notes) throws RazorpayException {
        validateCredentials();
        JSONObject request = new JSONObject();
        request.put("amount", amount);
        if (speed != null && !speed.isBlank()) {
            request.put("speed", speed);
        }
        if (receipt != null && !receipt.isBlank()) {
            request.put("receipt", receipt);
        }
        if (notes != null) {
            request.put("notes", new JSONObject(notes));
        }
        RazorpayClient razorpayClient = razorpayClientProvider.getClient();
        razorpayClient.addHeaders(Map.of("X-Refund-Idempotency", idempotencyKey));
        RazorpayRefundResponse response = RazorpayMapper
                .toRefundResponse(razorpayClient.payments.refund(paymentId, request));
        persist("refund", response.getId(), response);
        return response;
    }

    private void validateCredentials() {
        if (properties.keyId() == null || properties.keyId().isBlank()
                || properties.keySecret() == null || properties.keySecret().isBlank()) {
            throw new IllegalStateException("Razorpay credentials are not configured");
        }
    }

    private void persist(String resourceType, String providerId, Object payload) {
        if (providerId == null || providerId.isBlank()) {
            return;
        }
        try {
            ProviderData data = providerDataRepository
                    .findByResourceTypeAndProviderId(resourceType, providerId)
                    .orElseGet(ProviderData::new);
            data.setResourceType(resourceType);
            data.setProviderId(providerId);
            data.setPayload(objectMapper.writeValueAsString(payload));
            providerDataRepository.save(data);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize provider data", ex);
        }
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(attributes, properties.keySecret());
        } catch (RazorpayException | RuntimeException ex) {
            return false;
        }
    }

    public record ProviderOrderResponse(String orderId, Object amount, String currency, String status, String receipt) {
    }
}