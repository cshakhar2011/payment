package com.payment.payment.repository;

import com.payment.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderReference(String orderReference);

    Optional<PaymentOrder> findByProviderOrderId(String providerOrderId);
}
