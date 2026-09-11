package com.payment.payment.service;

import com.payment.exception.PaymentProviderException;
import com.payment.payment.dto.CreateRazorpayRefundRequest;
import com.payment.payment.dto.RazorpayRefundResponse;
import com.payment.payment.entity.Payment;
import com.payment.payment.entity.Refund;
import com.payment.payment.repository.PaymentRepository;
import com.payment.payment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private final RazorpayService razorpayService;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    public RefundService(RazorpayService razorpayService, RefundRepository refundRepository,
            PaymentRepository paymentRepository) {
        this.razorpayService = razorpayService;
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public RazorpayRefundResponse createRefund(String paymentId, String idempotencyKey,
            CreateRazorpayRefundRequest request) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId is required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("X-Refund-Idempotency header is required");
        }
        try {
            RazorpayRefundResponse response = razorpayService.refundPayment(
                    paymentId, idempotencyKey, request.getAmount(), request.getSpeed(), request.getReceipt(),
                    request.getNotes());
            Payment payment = paymentRepository.findByProviderPaymentId(paymentId).orElse(null);
            refundRepository.save(Refund.builder()
                    .providerRefundId(response.getId())
                    .payment(payment)
                    .amount(response.getAmount() instanceof Number value ? value.longValue() : request.getAmount())
                    .currency(response.getCurrency())
                    .status(response.getStatus())
                    .receipt(response.getReceipt() == null ? null : response.getReceipt().toString())
                    .speedProcessed(response.getSpeedProcessed())
                    .speedRequested(response.getSpeedRequested())
                    .build());
            return response;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to refund payment with provider", ex);
        }
    }

    public RazorpayRefundResponse getRefund(String refundId) {
        if (refundId == null || refundId.isBlank()) {
            throw new IllegalArgumentException("refundId is required");
        }
        try {
            return razorpayService.getRefund(refundId);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to fetch refund from provider", ex);
        }
    }
}