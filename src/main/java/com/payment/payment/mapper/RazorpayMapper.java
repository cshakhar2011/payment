package com.payment.payment.mapper;

import com.payment.payment.dto.PaymentLinkResponse;
import com.payment.payment.dto.RazorpayDowntimeResponse;
import com.payment.payment.dto.RazorpayPaymentResponse;
import com.payment.payment.dto.RazorpayQrCodeResponse;
import com.payment.payment.dto.RazorpayOrderResponse;
import com.payment.payment.dto.RazorpayReconciliationResponse;
import com.payment.payment.dto.RazorpayRefundResponse;
import com.payment.payment.dto.RazorpaySettlementResponse;
import com.payment.payment.dto.RazorpayWebhookResponse;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class RazorpayMapper {
    private RazorpayMapper() {
    }

    public static RazorpayWebhookResponse toWebhookResponse(com.razorpay.Webhook source) {
        List<String> events = new ArrayList<>();
        Object eventValue = source.get("events");
        if (eventValue instanceof Iterable<?> values) {
            for (Object value : values) {
                events.add(String.valueOf(value));
            }
        }
        return new RazorpayWebhookResponse(source.get("id"), source.get("created_at"), source.get("service"),
                source.get("owner_id"), source.get("owner_type"), source.get("context"), source.get("url"),
                source.get("alert_email"), source.get("secret_exists"), source.get("entity"), source.get("active"),
                events, source.get("disabled_at"));
    }

    public static RazorpayOrderResponse toOrderResponse(com.razorpay.Order source) {
        return RazorpayOrderResponse.builder()
                .id(source.get("id"))
                .amount(decimal(source.get("amount")))
                .amountPaid(decimal(source.get("amount_paid")))
                .amountDue(decimal(source.get("amount_due")))
                .currency(source.get("currency"))
                .receipt(source.get("receipt"))
                .status(source.get("status"))
                .attempts(number(source.get("attempts")))
                .notes(source.get("notes") == null ? null : source.get("notes").toString())
                .createdAt(date(source.get("created_at")))
                .payments(source.get("payments"))
                .build();
    }

    public static RazorpaySettlementResponse.SettlementItem toSettlementItem(com.razorpay.Settlement source) {
        return new RazorpaySettlementResponse.SettlementItem(source.get("id"), source.get("entity"),
                source.get("amount"), source.get("status"), source.get("fees"), source.get("tax"),
                source.get("utr"), source.get("created_at"));
    }

    public static RazorpayReconciliationResponse.ReconciliationItem toReconciliationItem(
            com.razorpay.Settlement source) {
        return new RazorpayReconciliationResponse.ReconciliationItem(source.get("entity_id"), source.get("type"),
                source.get("debit"), source.get("credit"), source.get("amount"), source.get("currency"),
                source.get("fee"), source.get("tax"), source.get("on_hold"), source.get("settled"),
                source.get("created_at"), source.get("settled_at"), source.get("settlement_id"),
                source.get("posted_at"), source.get("credit_type"), source.get("description"), source.get("notes"),
                source.get("payment_id"), source.get("settlement_utr"), source.get("order_id"),
                source.get("order_receipt"), source.get("method"), source.get("card_network"),
                source.get("card_issuer"), source.get("card_type"), source.get("dispute_id"));
    }

    private static BigDecimal decimal(Object value) {
        return value instanceof Number number ? BigDecimal.valueOf(number.doubleValue()) : null;
    }

    private static Long number(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static OffsetDateTime date(Object value) {
        return value instanceof Number number
                ? Instant.ofEpochSecond(number.longValue()).atOffset(ZoneOffset.UTC)
                : null;
    }

    public static PaymentLinkResponse toPaymentLinkResponse(PaymentLink source) {
        return new PaymentLinkResponse(source.get("upi_link"), source.get("accept_partial"), source.get("amount"),
                source.get("amount_paid"), source.get("callback_method"), source.get("callback_url"),
                source.get("cancelled_at"), source.get("created_at"), source.get("currency"), source.get("customer"),
                source.get("description"), source.get("expire_by"), source.get("expired_at"),
                source.get("first_min_partial_amount"), source.get("id"), source.get("notes"), source.get("notify"),
                source.get("payments"), source.get("reference_id"), source.get("reminder_enable"),
                source.get("reminders"), source.get("short_url"), source.get("status"), source.get("updated_at"),
                source.get("user_id"));
    }

    public static RazorpayQrCodeResponse toQrCodeResponse(com.razorpay.QrCode source) {
        return new RazorpayQrCodeResponse(source.get("id"), source.get("entity"), source.get("created_at"),
                source.get("name"), source.get("usage"), source.get("type"), source.get("image_url"),
                source.get("payment_amount"), source.get("status"), source.get("description"),
                source.get("fixed_amount"), source.get("payments_amount_received"),
                source.get("payments_count_received"), source.get("notes"), source.get("customer_id"),
                source.get("close_by"), source.get("closed_at"), source.get("close_reason"));
    }

    public static RazorpayDowntimeResponse.RazorpayDowntimeItem toDowntimeItem(Payment source) {
        return new RazorpayDowntimeResponse.RazorpayDowntimeItem(source.get("id"), source.get("entity"),
                source.get("method"), source.get("begin"), source.get("end"), source.get("status"),
                source.get("scheduled"), source.get("severity"), source.get("instrument"), source.get("created_at"),
                source.get("updated_at"));
    }

    public static RazorpayPaymentResponse toPaymentResponse(Payment source) {
        return new RazorpayPaymentResponse(source.get("id"), source.get("entity"), source.get("amount"),
                source.get("currency"), source.get("status"), source.get("order_id"), source.get("invoice_id"),
                source.get("terminal_id"), source.get("international"), source.get("method"),
                source.get("amount_refunded"), source.get("refund_status"), source.get("captured"),
                source.get("description"), source.get("card_id"), source.get("card"), source.get("bank"),
                source.get("wallet"), source.get("vpa"), source.get("email"), source.get("contact"),
                source.get("notes"), source.get("fee"), source.get("tax"), source.get("error_code"),
                source.get("error_description"), source.get("error_source"), source.get("error_step"),
                source.get("error_reason"), source.get("acquirer_data"), source.get("created_at"), source.get("upi"),
                source.get("customer_id"), source.get("token_id"), source.get("provider"), source.get("reward"));
    }

    public static RazorpayRefundResponse toRefundResponse(com.razorpay.Refund source) {
        return new RazorpayRefundResponse(source.get("id"), source.get("entity"), source.get("amount"),
                source.get("currency"), source.get("payment_id"), source.get("notes"), source.get("receipt"),
                source.get("acquirer_data"), source.get("created_at"), source.get("batch_id"), source.get("status"),
                source.get("speed_processed"), source.get("speed_requested"));
    }
}
