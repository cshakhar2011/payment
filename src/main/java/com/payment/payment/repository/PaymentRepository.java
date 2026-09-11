package com.payment.payment.repository;

import com.payment.payment.entity.Payment;
import com.payment.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentReference(String reference);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Optional<Payment> findByOrder(PaymentOrder order);
}
