package com.payment.payment.service;

import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.dto.MakePaymentRequest;
import com.payment.payment.dto.PaymentOrderResponse;
import com.payment.exception.PaymentProviderException;
import com.payment.payment.entity.OrderStatus;
import com.payment.payment.entity.Payment;
import com.payment.payment.entity.PaymentOrder;
import com.payment.payment.entity.PaymentStatus;
import com.payment.payment.repository.PaymentOrderRepository;
import com.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MakePaymentService {

    private final PaymentOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;

    @Transactional
    public PaymentResponse makePayment(String orderReference, MakePaymentRequest request) {
        PaymentOrder order = orderRepository.findByOrderReference(orderReference)
                .orElseThrow(() -> new IllegalArgumentException("Payment order was not found"));
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalArgumentException("Payment order is not awaiting payment");
        }

        if (order.getProviderOrderId() == null || order.getProviderOrderId().isBlank()) {
            throw new IllegalArgumentException("Payment order has no provider order ID");
        }
        Payment existingPayment = paymentRepository.findByOrder(order)
                .orElse(null);
        if (existingPayment != null) {
            return PaymentResponse.from(existingPayment);
        }
        if (!razorpayService.verifyPayment(order.getProviderOrderId(), request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            throw new IllegalArgumentException("Invalid payment signature");
        }

        Payment payment = Payment.builder()
                .paymentReference(orderReference)
                .order(order)
                .providerPaymentId(request.getRazorpayPaymentId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.AUTHORIZED)
                .build();
        order.setStatus(OrderStatus.AUTHORIZED);
        return PaymentResponse.from(paymentRepository.save(Objects.requireNonNull(payment)));
    }

    @Transactional
    public PaymentOrderResponse retryPayment(String orderReference) {
        PaymentOrder order = orderRepository.findByOrderReference(orderReference)
                .orElseThrow(() -> new IllegalArgumentException("Payment order was not found"));
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING && order.getStatus() != OrderStatus.FAILED) {
            throw new IllegalArgumentException("Payment order is not eligible for retry");
        }

        try {
            RazorpayService.ProviderOrderResponse providerOrder = razorpayService.createOrder(
                    order.getAmount(), order.getCurrency(), order.getOrderReference() + "-retry-" + System.nanoTime());
            order.setProviderOrderId(providerOrder.orderId());
            order.setStatus(OrderStatus.PAYMENT_PENDING);
            PaymentOrder savedOrder = orderRepository.save(order);
            return new PaymentOrderResponse(savedOrder.getId(), savedOrder.getOrderReference(),
                    savedOrder.getCustomerId(), savedOrder.getAmount(), savedOrder.getCurrency(),
                    savedOrder.getStatus().name(), savedOrder.getProvider(), savedOrder.getProviderOrderId());
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to create retry payment order with provider", ex);
        }
    }
}