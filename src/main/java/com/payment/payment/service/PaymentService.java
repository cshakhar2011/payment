package com.payment.payment.service;

import com.payment.exception.PaymentProviderException;
import com.payment.payment.dto.CapturePaymentRequest;
import com.payment.payment.dto.PaymentOrderResponse;
import com.payment.payment.entity.PaymentOrder;

import com.payment.payment.repository.PaymentOrderRepository;
import com.payment.payment.dto.VerifyPaymentRequest;
import com.payment.payment.dto.RazorpayPaymentResponse;
import com.payment.payment.dto.RazorpayDowntimeResponse;
import com.payment.payment.dto.RazorpaySettlementResponse;
import com.payment.payment.dto.RazorpayReconciliationResponse;
import com.payment.payment.dto.CreatePaymentLinkRequest;
import com.payment.payment.dto.PaymentLinkResponse;
import com.payment.payment.dto.RazorpayQrCodeResponse;
import com.payment.payment.dto.CreateQrCodeRequest;
import com.payment.payment.entity.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

        private final PaymentOrderRepository orderRepository;
        private final RazorpayService razorpayService;

        public PaymentService(PaymentOrderRepository orderRepository, RazorpayService razorpayService) {
                this.orderRepository = orderRepository;
                this.razorpayService = razorpayService;
        }

        public PaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request) {
                if (request.isAcceptPartial() && request.getFirstMinPartialAmount() == null) {
                        throw new IllegalArgumentException(
                                        "firstMinPartialAmount is required when acceptPartial is true");
                }
                try {
                        return razorpayService.createPaymentLink(request);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to create payment link with provider", ex);
                }
        }

        public PaymentLinkResponse createPaymentLinkForOrder(String orderReference) {
                PaymentOrder order = orderRepository.findByOrderReference(orderReference)
                                .orElseThrow(() -> new IllegalArgumentException("Payment order was not found"));
                if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
                        throw new IllegalArgumentException("Payment order is not awaiting payment");
                }
                if (order.getAmount() == null || order.getAmount().signum() <= 0) {
                        throw new IllegalArgumentException("Payment order amount must be greater than zero");
                }

                CreatePaymentLinkRequest request = new CreatePaymentLinkRequest();
                request.setAmount(order.getAmount().movePointRight(2).longValueExact());
                request.setCurrency(order.getCurrency());
                request.setReferenceId(orderReference);
                request.setDescription(order.getDescription() == null ? "Payment for " + orderReference
                                : order.getDescription());
                request.setCustomer(new CreatePaymentLinkRequest.Customer(orderReference, null, null));
                return createPaymentLink(request);
        }

        public RazorpayQrCodeResponse getQrCode(String qrCodeId) {
                if (qrCodeId == null || qrCodeId.isBlank()) {
                        throw new IllegalArgumentException("qrCodeId is required");
                }
                try {
                        return razorpayService.getQrCode(qrCodeId);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to fetch QR code from provider", ex);
                }
        }

        public RazorpayQrCodeResponse createQrCode(CreateQrCodeRequest request) {
                if (Boolean.TRUE.equals(request.getFixedAmount()) && request.getPaymentAmount() == null) {
                        throw new IllegalArgumentException("paymentAmount is required for a fixed amount QR code");
                }
                try {
                        return razorpayService.createQrCode(request);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to create QR code with provider", ex);
                }
        }

        public List<RazorpayPaymentResponse> listOrderPayments(String orderId) {
                if (orderId == null || orderId.isBlank()) {
                        throw new IllegalArgumentException("orderId is required");
                }
                try {
                        return razorpayService.listOrderPayments(orderId);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to list order payments from provider", ex);
                }
        }

        public RazorpayPaymentResponse getPayment(String paymentId) {
                if (paymentId == null || paymentId.isBlank()) {
                        throw new IllegalArgumentException("paymentId is required");
                }
                try {
                        return razorpayService.getPayment(paymentId);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to fetch payment from provider", ex);
                }
        }

        public RazorpayPaymentResponse capturePayment(String paymentId, CapturePaymentRequest request) {
                if (paymentId == null || paymentId.isBlank()) {
                        throw new IllegalArgumentException("paymentId is required");
                }
                try {
                        return razorpayService.capturePayment(paymentId, request.getAmount(), request.getCurrency());
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to capture payment with provider", ex);
                }
        }

        public RazorpayDowntimeResponse listDowntimes() {
                try {
                        return razorpayService.listDowntimes();
                } catch (Exception ex) {
                        return new RazorpayDowntimeResponse("collection", 0, List.of());
                }
        }

        public RazorpayDowntimeResponse.RazorpayDowntimeItem getDowntime(String downtimeId) {
                if (downtimeId == null || downtimeId.isBlank()) {
                        throw new IllegalArgumentException("downtimeId is required");
                }
                try {
                        return razorpayService.getDowntime(downtimeId);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to fetch payment downtime from provider", ex);
                }
        }

        public RazorpaySettlementResponse listSettlements(int count, int skip) {
                try {
                        return razorpayService.listSettlements(count, skip);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to list settlements from provider", ex);
                }
        }

        public RazorpaySettlementResponse.SettlementItem getSettlement(String settlementId) {
                if (settlementId == null || settlementId.isBlank()) {
                        throw new IllegalArgumentException("settlementId is required");
                }
                try {
                        return razorpayService.getSettlement(settlementId);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to fetch settlement from provider", ex);
                }
        }

        public RazorpayReconciliationResponse getCombinedReconciliation(int year, int month, int day) {
                try {
                        return razorpayService.getCombinedReconciliation(year, month, day);
                } catch (IllegalArgumentException ex) {
                        throw ex;
                } catch (Exception ex) {
                        throw new PaymentProviderException("Unable to fetch settlement reconciliation from provider",
                                        ex);
                }
        }

        @Transactional
        public PaymentOrderResponse verifyPayment(VerifyPaymentRequest request) {
                PaymentOrder order = orderRepository.findByProviderOrderId(request.getRazorpayOrderId())
                                .orElseThrow(() -> new IllegalArgumentException("Payment order was not found"));

                if (!razorpayService.verifyPayment(
                                request.getRazorpayOrderId(), request.getRazorpayPaymentId(),
                                request.getRazorpaySignature())) {
                        throw new IllegalArgumentException("Invalid payment signature");
                }

                return toResponse(order);
        }

        private PaymentOrderResponse toResponse(PaymentOrder order) {
                return new PaymentOrderResponse(
                                order.getId(), order.getOrderReference(), order.getCustomerId(), order.getAmount(),
                                order.getCurrency(), order.getStatus().name(), order.getProvider(),
                                order.getProviderOrderId());
        }
}
