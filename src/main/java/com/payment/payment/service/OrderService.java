package com.payment.payment.service;

import com.payment.exception.PaymentProviderException;
import com.payment.payment.dto.CreatePaymentOrderRequest;
import com.payment.payment.dto.PaymentOrderResponse;
import com.payment.payment.dto.RazorpayOrderResponse;
import com.payment.payment.dto.UpdateRazorpayOrderRequest;
import com.payment.payment.entity.OrderStatus;
import com.payment.payment.entity.PaymentOrder;
import com.payment.payment.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final PaymentOrderRepository orderRepository;
    private final RazorpayService razorpayService;

    public OrderService(PaymentOrderRepository orderRepository, RazorpayService razorpayService) {
        this.orderRepository = orderRepository;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public PaymentOrderResponse createOrder(CreatePaymentOrderRequest request) {
        // String reference = "ORD-" + UUID.randomUUID().toString().replace("-",
        // "").substring(0, 16).toUpperCase();
        String reference = "ORDER-" + request.getCustomerId();
        String currency = request.getCurrency().toUpperCase();
        RazorpayService.ProviderOrderResponse providerOrder;
        try {
            providerOrder = razorpayService.createOrder(request.getAmount(), currency, reference);
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to create payment order with provider", ex);
        }

        PaymentOrder order = PaymentOrder.builder()
                .orderReference(reference)
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(currency)
                .description(request.getDescription())
                .provider("RAZORPAY")
                .providerOrderId(providerOrder.orderId())
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        return toResponse(orderRepository.save(order));
    }

    public List<RazorpayOrderResponse> listOrders(int count, int skip) {
        try {
            return razorpayService.listOrders(count, skip);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            return orderRepository.findAll().stream()
                    .skip(skip)
                    .limit(count)
                    .map(this::toProviderOrderResponse)
                    .toList();
        }
    }

    private RazorpayOrderResponse toProviderOrderResponse(PaymentOrder order) {
        BigDecimal amountInPaise = order.getAmount().movePointRight(2);
        return RazorpayOrderResponse.builder()
                .id(order.getProviderOrderId())
                .amount(amountInPaise)
                .amountPaid(BigDecimal.ZERO)
                .amountDue(amountInPaise)
                .currency(order.getCurrency())
                .receipt(order.getOrderReference())
                .status(order.getStatus().name())
                .attempts(0L)
                .notes(null)
                .createdAt(order.getCreatedAt())
                .payments(null)
                .build();
    }

    public RazorpayOrderResponse getOrder(String orderId) {
        validateId(orderId);
        try {
            return razorpayService.getOrder(orderId);
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to fetch payment order from provider", ex);
        }
    }

    public RazorpayOrderResponse updateOrder(String orderId, UpdateRazorpayOrderRequest request) {
        validateId(orderId);
        try {
            return razorpayService.updateOrder(orderId, request.getNotes());
        } catch (Exception ex) {
            throw new PaymentProviderException("Unable to update payment order with provider", ex);
        }
    }

    private PaymentOrderResponse toResponse(PaymentOrder order) {
        return new PaymentOrderResponse(order.getId(), order.getOrderReference(), order.getCustomerId(),
                order.getAmount(), order.getCurrency(), order.getStatus().name(), order.getProvider(),
                order.getProviderOrderId());
    }

    private void validateId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }
    }
}